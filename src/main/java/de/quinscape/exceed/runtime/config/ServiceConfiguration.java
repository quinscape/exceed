package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.component.QueryDataProvider;
import de.quinscape.exceed.runtime.component.QueryExecutor;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.JOOQQueryExecutor;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.editor.completion.CompletionService;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.i18n.DefaultTranslator;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.resource.DefaultResourceCacheFactory;
import de.quinscape.exceed.runtime.resource.ResourceCacheFactory;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.DomainServiceFactory;
import de.quinscape.exceed.runtime.service.ProcessService;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ComponentScan(value = {
    "de.quinscape.exceed.runtime.service"
})
public class ServiceConfiguration
{
    @Bean
    public ViewDataService viewDataService()
    {
        return new ViewDataService();
    }

    @Bean
    public QueryTransformer queryTransformer(ExpressionService expressionService, NamingStrategy namingStrategy)
    {
        return new QueryTransformer(expressionService, namingStrategy);
    }

    @Bean
    public ResourceCacheFactory resourceCacheFactory()
    {
        DefaultResourceCacheFactory cacheFactory = new DefaultResourceCacheFactory();
        cacheFactory.setCacheSizePerApplication(10000);
        return cacheFactory;
    }

    private final static String DEFAULT_QUERY_EXECUTOR = "jooqQueryExecutor";

    @Bean(name = DEFAULT_QUERY_EXECUTOR)
    public JOOQQueryExecutor defaultQueryExecutor(DSLContext dslContext, NamingStrategy namingStrategy)
    {
        return new JOOQQueryExecutor(dslContext, namingStrategy);
    }

    @Bean
    public ModelDocsProvider modelDocsProvider()
    {
        return new ModelDocsProvider();
    }

    @Bean
    public NamingStrategy namingStrategy()
    {
        return new DefaultNamingStrategy();
    }

    @Bean
    public QueryDataProvider queryDataProvider(ApplicationContext applicationContext, QueryTransformer
        queryTransformer)
    {
        Map<String, QueryExecutor> executors = applicationContext.getBeansOfType(QueryExecutor.class);
        return new QueryDataProvider(queryTransformer, executors, DEFAULT_QUERY_EXECUTOR);
    }

    @Bean
    public Translator translator()
    {
        return new DefaultTranslator();
    }

    @Bean
    public DefaultPropertyConverters defaultPropertyConverters()
    {
        return new DefaultPropertyConverters();
    }

    @Bean
    public CompletionService completionService()
    {
        return new CompletionService();
    }

    @Bean
    public ScopedContextFactory scopedContextFactory(
        ApplicationService applicationService,
        ExpressionService expressionService,
        ActionService actionService
    )
    {
        return new ScopedContextFactory(applicationService, expressionService, actionService);
    }

    @Bean
    public RuntimeContextFactory runtimeContextFactory(Translator translator)
    {
        return new RuntimeContextFactory(translator);
    }

    @Bean
    public DomainServiceFactory domainServiceFactory(
        NamingStrategy namingStrategy,
        DSLContext dslContext,
        DefaultPropertyConverters defaultPropertyConverters,
    )
    {
        return new DomainServiceFactory(namingStrategy, dslContext, defaultPropertyConverters);
    }
    @Bean
    public ProcessService ProcessService(ActionService actionService, ExpressionService expressionService, ScopedContextFactory
        scopedContextFactory)
    {
        return new ProcessService(actionService, expressionService, scopedContextFactory);
    }

}
