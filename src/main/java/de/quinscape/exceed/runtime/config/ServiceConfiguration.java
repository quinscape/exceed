package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.component.QueryDataProvider;
import de.quinscape.exceed.runtime.component.QueryExecutor;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.JOOQQueryExecutor;
import de.quinscape.exceed.runtime.domain.ModelQueryExecutor;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.editor.completion.CompletionService;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.i18n.DefaultTranslator;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.resource.DefaultResourceCacheFactory;
import de.quinscape.exceed.runtime.resource.ResourceCacheFactory;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
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
import org.springframework.transaction.PlatformTransactionManager;

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
    public ResourceCacheFactory resourceCacheFactory()
    {
        DefaultResourceCacheFactory cacheFactory = new DefaultResourceCacheFactory();
        cacheFactory.setCacheSizePerApplication(10000);
        return cacheFactory;
    }

    @Bean
    public ModelDocsProvider modelDocsProvider()
    {
        return new ModelDocsProvider();
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
        DefaultPropertyConverters defaultPropertyConverters,
        StorageConfigurationRepository storageConfigurationRepository
    )
    {
        return new DomainServiceFactory(defaultPropertyConverters,storageConfigurationRepository);
    }
    @Bean
    public ProcessService ProcessService(ActionService actionService, ExpressionService expressionService, ScopedContextFactory
        scopedContextFactory)
    {
        return new ProcessService(actionService, expressionService, scopedContextFactory);
    }

}
