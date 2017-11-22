package de.quinscape.exceed.runtime.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.component.QueryDataProvider;
import de.quinscape.exceed.runtime.component.TestDataProvider;
import de.quinscape.exceed.runtime.datasrc.AtomikosDataSourceFactory;
import de.quinscape.exceed.runtime.datasrc.JOOQDataSourceFactory;
import de.quinscape.exceed.runtime.datasrc.QueryTypeDataSourceFactory;
import de.quinscape.exceed.runtime.datasrc.SystemDataSourceModelFactory;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.DefaultQueryParameterProvider;
import de.quinscape.exceed.runtime.domain.DefaultQueryTypeSQLFactory;
import de.quinscape.exceed.runtime.domain.JOOQDomainOperations;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.domain.NeutralNamingStrategy;
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
import de.quinscape.exceed.runtime.schema.InformationSchemaOperations;
import de.quinscape.exceed.runtime.schema.NoopSchemaService;
import de.quinscape.exceed.runtime.security.ExceedTokenRepository;
import de.quinscape.exceed.runtime.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.servlet.ServletContext;
import javax.transaction.SystemException;

@Configuration
@EnableTransactionManagement
@PropertySource(value = "classpath:application.properties")
public class DomainConfiguration
{
    private final static Logger log = LoggerFactory.getLogger(DomainConfiguration.class);

    private final ApplicationContext applicationContext;

    private final Environment env;

    @Autowired
    public DomainConfiguration(
        ApplicationContext applicationContext,
        Environment env
    )
    {
        this.applicationContext = applicationContext;
        this.env = env;
    }

//    @Bean
//    public PropertyDefaultOperations propertyDefaultOperations()
//    {
//        return new PropertyDefaultOperations();
//    }

//    @Bean
//    public DataSource transactionAwareDataSourceProxy(Environment environment,
//        ApplicationService applicationService
//    )
//    {
//        log.info("Creating data source: {}", applicationService);
//
//        BoneCPDataSource src = new BoneCPDataSource();
//        src.setDriverClass(environment.getProperty("database.driver"));
//        src.setJdbcUrl(environment.getProperty("database.url"));
//        src.setUsername(environment.getProperty("database.username"));
//        src.setPassword(environment.getProperty("database.password"));
//
//        // pool config
//        src.setIdleConnectionTestPeriod(60, TimeUnit.SECONDS);
//        src.setMaxConnectionsPerPartition(4);
//        src.setMinConnectionsPerPartition(2);
//        src.setPartitionCount(1);
//        src.setStatementsCacheSize(100);
//        return new TransactionAwareDataSourceProxy(src);
//    }
//

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
        ActionService actionService
    )
    {
        return new QueryDataProvider(actionService);
    }

    @Bean
    public TestDataProvider testDataProvider()
    {
        return new TestDataProvider();
    }


//    @Bean(name = DomainType.SYSTEM_STORAGE)
//    public StorageConfiguration systemStorage()
//    {
//        return new DefaultStorageConfiguration(, new NeutralNamingStrategy(), null);
//    }

    @Bean
    public SystemStorageOperations systemStorageOperations()
    {
        return new SystemStorageOperations();
    }


    @Bean
    public DefaultNamingStrategy defaultNamingStrategy()
    {
        return new DefaultNamingStrategy();
    }

    @Bean
    public NeutralNamingStrategy neutralNamingStrategy()
    {
        return new NeutralNamingStrategy();
    }

//    @Bean(name = DomainType.DEFAULT_STORAGE)
//    public StorageConfiguration jooqDatabaseStorage(
//        JOOQDomainOperations jooqDomainOperations,
//        DefaultSchemaService defaultSchemaService
//    )
//    {
//        return new DefaultStorageConfiguration(
//            jooqDomainOperations,
//            new DefaultNamingStrategy(),
//            defaultSchemaService
//        );
//    }

//    @Bean(name = QueryTypeModel.DEFAULT_QUERY_STORAGE)
//    public StorageConfiguration queryTypeStorageConfiguration(
//        DefaultSchemaService defaultSchemaService,
//        QueryTypeOperations queryTypeOperations
//    )
//    {
//        return new DefaultStorageConfiguration(
//            queryTypeOperations,
//            new DefaultNamingStrategy(),
//            null
//        );
//    }

    @Bean
    public QueryTypeOperations queryTypeOperations(
    )
    {
        return new QueryTypeOperations(
            applicationContext.getBeansOfType(SqlQueryFactory.class),
            applicationContext.getBeansOfType(QueryTypeParameterProvider.class),
            applicationContext.getBeansOfType(QueryTypeUpdateHandler.class)
        );
    }
    
    @Bean
    public ComponentQueryTransformer queryTransformer(ExpressionService expressionService)
    {
        return new ComponentQueryTransformer(expressionService);
    }

    @Bean
    public JOOQDomainOperations jooqDomainOperations(
    ) throws SystemException
    {
        return new JOOQDomainOperations(jtaTransactionManager());
    }

    @Bean
    public DefaultSchemaService defaultSchemaService()
    {
        final DefaultNamingStrategy namingStrategy = new DefaultNamingStrategy();
        return new DefaultSchemaService(
            namingStrategy,
            (runtimeContext, dataSource) ->
                new InformationSchemaOperations(
                    env,
                    runtimeContext.getApplicationModel().getName(),
                    namingStrategy,
                    dataSource
                )
        );
    }

    @Bean
    public NoopSchemaService noopSchemaService()
    {
        return new NoopSchemaService();
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

    @Bean
    public AtomikosDataSourceFactory atomikosDataSourceFactory()
    {
        return new AtomikosDataSourceFactory();
    }

    @Bean(
        initMethod = "init",
        destroyMethod = "close"
    )
    public UserTransactionManager userTransactionManager()
    {
        final UserTransactionManager manager = new UserTransactionManager();
        manager.setForceShutdown(false);
        return manager;
    }

    @Bean
    public UserTransactionImp userTransactionImp() throws SystemException
    {
        final UserTransactionImp transactionImp = new UserTransactionImp();
        transactionImp.setTransactionTimeout(300);
        return transactionImp;
    }

    @Bean
    public JtaTransactionManager jtaTransactionManager() throws SystemException
    {
        final JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager());
        jtaTransactionManager.setUserTransaction(userTransactionImp());
        jtaTransactionManager.setAllowCustomIsolationLevels(true);
        return jtaTransactionManager;
    }

    @Bean
    public JOOQDataSourceFactory jooqDataSourceFactory()
    {
        return new JOOQDataSourceFactory();
    }

    @Bean
    public SystemDataSourceModelFactory systemDataSourceModelFactory()
    {
        return new SystemDataSourceModelFactory();
    }

    @Bean
    public QueryTypeDataSourceFactory queryTypeDataSourceFactory()
    {
        return new QueryTypeDataSourceFactory();
    }

}
