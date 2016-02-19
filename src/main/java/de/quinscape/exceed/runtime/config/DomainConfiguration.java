package de.quinscape.exceed.runtime.config;

import com.jolbox.bonecp.BoneCPDataSource;
import de.quinscape.exceed.runtime.db.JOOQConfigFactory;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultDSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import de.quinscape.exceed.runtime.domain.property.*;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableTransactionManagement
@PropertySource(value = "classpath:application.properties")
public class DomainConfiguration
{
    private static Logger log = LoggerFactory.getLogger(DomainConfiguration.class);


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
    public DSLContext dslContext(JOOQConfigFactory configFactory)
    {
        org.jooq.Configuration configuration = configFactory.create();
        DefaultDSLContext defaultDSLContext = new DefaultDSLContext(configuration);

        log.info("Created DSLContext", defaultDSLContext);

        return defaultDSLContext;
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
    public BooleanConverter BooleanConverter()
    {
        return new BooleanConverter();
    }


    @Bean
    public DateConverter DateConverter()
    {
        return new DateConverter();
    }


    @Bean
    public EnumConverter EnumConverter()
    {
        return new EnumConverter();
    }


    @Bean
    public IntegerConverter IntegerConverter()
    {
        return new IntegerConverter();
    }


    @Bean
    public LongConverter LongConverter()
    {
        return new LongConverter();
    }


    @Bean
    public PlainTextConverter PlainTextConverter()
    {
        return new PlainTextConverter();
    }


    @Bean
    public RichTextConverter RichTextConverter()
    {
        return new RichTextConverter();
    }


    @Bean
    public TimestampConverter TimestampConverter()
    {
        return new TimestampConverter();
    }


    @Bean
    public UUIDConverter UUIDConverter()
    {
        return new UUIDConverter();
    }


    @Bean
    public ObjectConverter ObjectConverter()
    {
        return new ObjectConverter();
    }
}
