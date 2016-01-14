package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.component.QueryDataProvider;
import de.quinscape.exceed.runtime.component.QueryExecutor;
import de.quinscape.exceed.runtime.controller.ActionRegistry;
import de.quinscape.exceed.runtime.controller.DefaultActionRegistry;
import de.quinscape.exceed.runtime.datalist.DataListService;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.JOOQQueryExecutor;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
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
import org.springframework.context.annotation.FilterType;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(value = {
    "de.quinscape.exceed.runtime.action",
    "de.quinscape.exceed.runtime.service"
}, includeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = Action.class)
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
    public ActionRegistry actionRegistry()
    {
        Map<String, Action> actions = new HashMap<>();

        for (Action action : applicationContext.getBeansOfType(Action.class).values())
        {
            // instantiate the action model per mandatory default constructor
            ActionModel actionModel = null;
            try
            {
                actionModel = (ActionModel) action.getActionModelClass().newInstance();
            }
            catch (Exception e)
            {
                throw new ExceedRuntimeException("Error creating action model for " + action );
            }
            // .. and register the action under the name provided by the model.
            actions.put(actionModel.getAction(), action);
        }

        return new DefaultActionRegistry(actions);
    }

    @Bean
    public Translator translator()
    {
        return new DefaultTranslator();
    }

    @Bean
    public DataListService dataListService(ApplicationContext applicationContext)
    {
        if (applicationContext == null)
        {
            throw new IllegalArgumentException("applicationContext can't be null");
        }


        Map<String, PropertyConverter> propertyTypes = applicationContext.getBeansOfType(PropertyConverter.class);
        return new DataListService(propertyTypes);
    }
}
