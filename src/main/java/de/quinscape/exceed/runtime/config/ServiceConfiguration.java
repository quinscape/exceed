package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.component.QueryDataProvider;
import de.quinscape.exceed.runtime.component.QueryExecutor;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.JOOQQueryExecutor;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.i18n.DefaultTranslator;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.resource.DefaultResourceCacheFactory;
import de.quinscape.exceed.runtime.resource.ResourceCacheFactory;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ComponentScan({
    "de.quinscape.exceed.runtime.service"
})
public class ServiceConfiguration
{
    @Autowired
    private ComponentRegistry componentRegistry;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ApplicationContext applicationContext;

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
    public QueryDataProvider defaultDataProvider(ApplicationContext applicationContext)
    {
        Map<String, QueryExecutor> executors = applicationContext.getBeansOfType(QueryExecutor.class);
        return new QueryDataProvider(dslContext, new QueryTransformer(), executors, DEFAULT_QUERY_EXECUTOR);
    }
    @Bean
    public Translator translator()
    {
        return new DefaultTranslator();
    }
}
