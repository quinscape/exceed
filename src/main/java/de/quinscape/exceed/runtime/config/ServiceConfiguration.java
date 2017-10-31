package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.domain.migration.MigrationStep;
import de.quinscape.exceed.runtime.domain.migration.MigrationStepRepository;
import de.quinscape.exceed.runtime.editor.completion.CompletionService;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.query.ComponentQueryTransformer;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.resource.DefaultResourceCacheFactory;
import de.quinscape.exceed.runtime.resource.ResourceCacheFactory;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.service.DomainServiceFactory;
import de.quinscape.exceed.runtime.service.ProcessService;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final static Logger log = LoggerFactory.getLogger(ServiceConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;


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
    public CompletionService completionService()
    {
        return new CompletionService();
    }


    @Bean
    public ScopedContextFactory scopedContextFactory(
    )
    {
        return new ScopedContextFactory();
    }


    @Bean
    public RuntimeContextFactory runtimeContextFactory(Translator translator)
    {
        return new RuntimeContextFactory(translator);
    }


    @Bean
    public DomainServiceFactory domainServiceFactory(
        StorageConfigurationRepository storageConfigurationRepository
    )
    {

        return new DomainServiceFactory(
            storageConfigurationRepository
        );
    }


    @Bean
    public ProcessService ProcessService(ScopedContextFactory scopedContextFactory)
    {
        return new ProcessService(scopedContextFactory);
    }

    @Bean
    public MigrationStepRepository migrationStepRepository()
    {
        final Map<String, MigrationStep> beansOfType = applicationContext.getBeansOfType(MigrationStep.class);
        return new MigrationStepRepository(beansOfType);
    }

    @Bean
    public ComponentQueryTransformer componentQueryTransformer(
        ExpressionService expressionService,
        StorageConfigurationRepository storageConfigurationRepository)
    {
         return new ComponentQueryTransformer(
             expressionService,
             storageConfigurationRepository
         );
    }
}
