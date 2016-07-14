package de.quinscape.exceed.runtime.config;

import com.jolbox.bonecp.BoneCPDataSource;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.component.QueryDataProvider;
import de.quinscape.exceed.runtime.component.domain.DomainEditorProvider;
import de.quinscape.exceed.runtime.component.translation.TranslationEditorProvider;
import de.quinscape.exceed.runtime.db.JOOQConfigFactory;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.JOOQDomainOperations;
import de.quinscape.exceed.runtime.domain.JOOQQueryExecutor;
import de.quinscape.exceed.runtime.domain.SystemStorageExecutor;
import de.quinscape.exceed.runtime.domain.NeutralNamingStrategy;
import de.quinscape.exceed.runtime.domain.PropertyDefaultOperations;
import de.quinscape.exceed.runtime.domain.SystemStorageOperations;
import de.quinscape.exceed.runtime.domain.property.BooleanConverter;
import de.quinscape.exceed.runtime.domain.property.DateConverter;
import de.quinscape.exceed.runtime.domain.property.EnumConverter;
import de.quinscape.exceed.runtime.domain.property.IntegerConverter;
import de.quinscape.exceed.runtime.domain.property.LongConverter;
import de.quinscape.exceed.runtime.domain.property.ObjectConverter;
import de.quinscape.exceed.runtime.domain.property.PlainTextConverter;
import de.quinscape.exceed.runtime.domain.property.RichTextConverter;
import de.quinscape.exceed.runtime.domain.property.TimestampConverter;
import de.quinscape.exceed.runtime.domain.property.UUIDConverter;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.i18n.DefaultTranslator;
import de.quinscape.exceed.runtime.i18n.JOOQTranslationProvider;
import de.quinscape.exceed.runtime.i18n.TranslationProvider;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.schema.DefaultSchemaService;
import de.quinscape.exceed.runtime.schema.DefaultStorageConfiguration;
import de.quinscape.exceed.runtime.schema.DefaultStorageConfigurationRepository;
import de.quinscape.exceed.runtime.schema.InformationSchemaOperations;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
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
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
    public QueryDataProvider queryDataProvider(QueryTransformer queryTransformer, StorageConfigurationRepository storageConfigurationRepository)
    {
        return new QueryDataProvider(queryTransformer, storageConfigurationRepository);
    }

    @Bean
    public SystemStorageExecutor systemStorageExecutor()
    {
        return new SystemStorageExecutor();
    }

    @Bean
    public StorageConfigurationRepository storageConfigurationRepository()
    {
        final Map<String, StorageConfiguration> configurations = applicationContext.getBeansOfType(StorageConfiguration.class);

        log.info("STORAGE CONFIGURATIONS: {}", configurations);

        return new DefaultStorageConfigurationRepository(configurations, DomainType.DEFAULT_STORAGE);
    }

    @Bean(name = DomainType.SYSTEM_STORAGE)
    public StorageConfiguration systemStorage(SystemStorageExecutor systemStorageExecutor)
    {
        return new DefaultStorageConfiguration(new SystemStorageOperations(), new NeutralNamingStrategy(), systemStorageExecutor, null);
    }

    @Bean(name = DomainType.DEFAULT_STORAGE)
    public StorageConfiguration jooqDatabaseStorage(
        DSLContext dslContext,
        JOOQDomainOperations jooqDomainOperations,
        DefaultSchemaService defaultSchemaService
    )
    {
        final JOOQQueryExecutor jooqQueryExecutor = new JOOQQueryExecutor(dslContext);
        return new DefaultStorageConfiguration(
            jooqDomainOperations,
            new DefaultNamingStrategy(),
            jooqQueryExecutor,
            defaultSchemaService);
    }

    @Bean
    public QueryTransformer queryTransformer(ExpressionService expressionService, StorageConfigurationRepository storageConfigurationRepository)
    {
        return new QueryTransformer(expressionService, storageConfigurationRepository);
    }

    @Bean
    public JOOQDomainOperations jooqDomainOperations(DSLContext dslContext, ExpressionService expressionService, PlatformTransactionManager txManager)
    {
        return new JOOQDomainOperations(dslContext, expressionService, txManager);
    }

    @Bean
    public DefaultSchemaService defaultSchemaService(DataSource dataSource)
    {
        final DefaultNamingStrategy namingStrategy = new DefaultNamingStrategy();
        return new DefaultSchemaService(namingStrategy, new InformationSchemaOperations(dataSource, namingStrategy));
    }

    @Bean
    public TranslationProvider jooQTranslationProvider(DSLContext dslContext)
    {
        return new JOOQTranslationProvider(dslContext);
    }


    @Bean
    public Translator translator(TranslationProvider provider)
    {
        return new DefaultTranslator(provider);
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
