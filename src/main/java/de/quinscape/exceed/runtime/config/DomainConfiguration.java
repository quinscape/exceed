package de.quinscape.exceed.runtime.config;

import com.jolbox.bonecp.BoneCPDataSource;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.component.QueryDataProvider;
import de.quinscape.exceed.runtime.component.TestDataProvider;
import de.quinscape.exceed.runtime.db.JOOQConfigFactory;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.DefaultQueryParameterProvider;
import de.quinscape.exceed.runtime.domain.DefaultQueryTypeSQLFactory;
import de.quinscape.exceed.runtime.domain.JOOQDomainOperations;
import de.quinscape.exceed.runtime.domain.NeutralNamingStrategy;
import de.quinscape.exceed.runtime.domain.PropertyDefaultOperations;
import de.quinscape.exceed.runtime.domain.QueryTypeOperations;
import de.quinscape.exceed.runtime.domain.QueryTypeParameterProvider;
import de.quinscape.exceed.runtime.domain.QueryTypeUpdateHandler;
import de.quinscape.exceed.runtime.domain.SqlQueryFactory;
import de.quinscape.exceed.runtime.domain.SystemStorageOperations;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.query.ComponentQueryTransformer;
import de.quinscape.exceed.runtime.i18n.DefaultTranslator;
import de.quinscape.exceed.runtime.i18n.ExceedAppTranslationProvider;
import de.quinscape.exceed.runtime.i18n.TranslationProvider;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.schema.DefaultSchemaService;
import de.quinscape.exceed.runtime.schema.DefaultStorageConfiguration;
import de.quinscape.exceed.runtime.schema.DefaultStorageConfigurationRepository;
import de.quinscape.exceed.runtime.schema.InformationSchemaOperations;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import de.quinscape.exceed.runtime.security.ExceedTokenRepository;
import de.quinscape.exceed.runtime.service.ApplicationService;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultDSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableTransactionManagement
@PropertySource(value = "classpath:application.properties")
public class DomainConfiguration
{
    private final static Logger log = LoggerFactory.getLogger(DomainConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PropertyDefaultOperations propertyDefaultOperations()
    {
        return new PropertyDefaultOperations();
    }

    @Bean
    public DataSource transactionAwareDataSourceProxy(Environment environment)
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
    public ExceedTokenRepository tokenRepository(
        ServletContext servletContext,
        ApplicationService applicationService
    )
    {
        return new ExceedTokenRepository(servletContext, applicationService);
    }


    @Bean
    public QueryDataProvider queryDataProvider(
        StorageConfigurationRepository storageConfigurationRepository,
        ActionService actionService
    )
    {
        return new QueryDataProvider(storageConfigurationRepository, actionService);
    }

    @Bean
    public TestDataProvider testDataProvider()
    {
        return new TestDataProvider();
    }


    @Bean
    public StorageConfigurationRepository storageConfigurationRepository()
    {
        final Map<String, StorageConfiguration> configurations = applicationContext.getBeansOfType(StorageConfiguration.class);

        log.info("STORAGE CONFIGURATIONS: {}", configurations);

        return new DefaultStorageConfigurationRepository(configurations, DomainType.DEFAULT_STORAGE);
    }

    @Bean(name = DomainType.SYSTEM_STORAGE)
    public StorageConfiguration systemStorage()
    {
        return new DefaultStorageConfiguration(new SystemStorageOperations(), new NeutralNamingStrategy(), null);
    }

    @Bean(name = DomainType.DEFAULT_STORAGE)
    public StorageConfiguration jooqDatabaseStorage(
        JOOQDomainOperations jooqDomainOperations,
        DefaultSchemaService defaultSchemaService
    )
    {
        return new DefaultStorageConfiguration(
            jooqDomainOperations,
            new DefaultNamingStrategy(),
            defaultSchemaService
        );
    }

    @Bean(name = QueryTypeModel.DEFAULT_QUERY_STORAGE)
    public StorageConfiguration queryTypeStorageConfiguration(
        DefaultSchemaService defaultSchemaService,
        QueryTypeOperations queryTypeOperations
    )
    {
        return new DefaultStorageConfiguration(
            queryTypeOperations,
            new DefaultNamingStrategy(),
            null
        );
    }

    @Bean
    public QueryTypeOperations queryTypeOperations(
        JdbcTemplate jdbcTemplate
    )
    {
        return new QueryTypeOperations(
            jdbcTemplate,
            applicationContext.getBeansOfType(SqlQueryFactory.class),
            applicationContext.getBeansOfType(QueryTypeParameterProvider.class),
            applicationContext.getBeansOfType(QueryTypeUpdateHandler.class)
        );
    }
    
    @Bean
    public ComponentQueryTransformer queryTransformer(ExpressionService expressionService, StorageConfigurationRepository storageConfigurationRepository)
    {
        return new ComponentQueryTransformer(expressionService, storageConfigurationRepository);
    }

    @Bean
    public JOOQDomainOperations jooqDomainOperations(
        DSLContext dslContext,
        PlatformTransactionManager txManager
    )
    {
        return new JOOQDomainOperations(dslContext, txManager);
    }

    @Bean
    public DefaultSchemaService defaultSchemaService(
        DataSource dataSource
    )
    {
        final DefaultNamingStrategy namingStrategy = new DefaultNamingStrategy();
        return new DefaultSchemaService(namingStrategy, new InformationSchemaOperations(dataSource, namingStrategy));
    }

    @Bean
    public TranslationProvider jooQTranslationProvider()
    {
        return new ExceedAppTranslationProvider();
    }


    @Bean
    public Translator translator(TranslationProvider provider)
    {
        return new DefaultTranslator(provider);
    }

    @Bean
    public DefaultQueryTypeSQLFactory defaultQueryTypeSQLFactory()
    {
        return new DefaultQueryTypeSQLFactory();
    }

    @Bean
    public DefaultQueryParameterProvider defaultQueryParameterProvider()
    {
        return new DefaultQueryParameterProvider();
    }

}
