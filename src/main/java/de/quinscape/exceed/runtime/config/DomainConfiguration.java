package de.quinscape.exceed.runtime.config;

import com.jolbox.bonecp.BoneCPDataSource;
import de.quinscape.exceed.domain.tables.AppUser;
import de.quinscape.exceed.runtime.db.JOOQConfigFactory;
import de.quinscape.exceed.runtime.domain.DomainServiceImpl;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainRegistration;
import de.quinscape.exceed.runtime.domain.DomainRegistry;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableTransactionManagement
@PropertySource(value = "classpath:application.properties")
@ComponentScan({
    "de.quinscape.exceed.runtime.service"
})
public class DomainConfiguration
{
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSourceProxy(Environment environment)
    {
        BoneCPDataSource src = new BoneCPDataSource();
        src.setDriverClass(environment.getProperty("database.driver"));
        src.setJdbcUrl(environment.getProperty("database.url"));
        src.setUsername(environment.getProperty("database.username"));
        src.setPassword(environment.getProperty("database.password"));

        // pool config
        src.setIdleConnectionTestPeriod(60, TimeUnit.SECONDS);
        src.setMaxConnectionsPerPartition(30);
        src.setMinConnectionsPerPartition(10);
        src.setPartitionCount(3);
        src.setAcquireIncrement(5);
        src.setStatementsCacheSize(100);
        return new TransactionAwareDataSourceProxy(src);
    }
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource)
    {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DefaultDSLContext dslContext(JOOQConfigFactory configFactory)
    {
        org.jooq.Configuration configuration = configFactory.create();
        return new DefaultDSLContext(configuration);
    }

    @Bean
    public JOOQConfigFactory jooqConfigFactory(DataSourceConnectionProvider connectionProvider)
    {
        JOOQConfigFactory factory = new JOOQConfigFactory();
        factory.setConnectionProvider(connectionProvider);
        factory.setDialect(SQLDialect.POSTGRES);

        return factory;
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider(DataSource dataSource)
    {
        return new DataSourceConnectionProvider(dataSource);
    }

    @Bean
    public JdbcTokenRepositoryImpl tokenRepository(DataSource dataSource)
    {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    @Bean
    public DomainService domainJSONService() throws Exception
    {
        DomainService svc = new DomainServiceImpl();
        DomainRegistry registry = svc.getRegistry();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AssignableTypeFilter(DomainObject.class));
        Set<BeanDefinition> candidates = provider.findCandidateComponents(AppUser.class.getPackage().getName());
        for (BeanDefinition candidate : candidates)
        {
            Class<?> domainObjectClass = Class.forName(candidate.getBeanClassName());

            DomainObject test = (DomainObject) domainObjectClass.newInstance();

            registry.register(test.getType(), (Class<? extends DomainObject>) domainObjectClass);
        }

        ServiceLoader<DomainRegistration> loader = ServiceLoader.load(DomainRegistration.class);
        for (DomainRegistration registration : loader)
        {
            registration.register(registry);
        }

        return svc;
    }

    @Bean
    public ModelCompositionService modelCompositionService()
    {
        ModelCompositionService svc = new ModelCompositionService();
        return svc;
    }
}
