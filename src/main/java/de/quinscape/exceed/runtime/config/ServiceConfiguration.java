package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.i18n.DefaultTranslator;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
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
    public QueryTransformer queryTransformer()
    {
        return new QueryTransformer();
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
    public JOOQQueryExecutor defaultQueryExecutor()
    {
        return new JOOQQueryExecutor(dslContext, new DefaultNamingStrategy());
    }

    @Bean
    public Translator translator()
    {
        return new DefaultTranslator();
    }
}
