package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.expression.TokenMgrError;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.view.DataProviderContext;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

public class QueryDataProvider
    implements DataProvider
{
    private static Logger log = LoggerFactory.getLogger(QueryDataProvider.class);

    private final DSLContext dslContext;
    private final QueryTransformer queryTransformer;

    private final Map<String, QueryExecutor> queryExecutors;

    private final String defaultExecutor;


    public QueryDataProvider(DSLContext dslContext, QueryTransformer queryTransformer, Map<String, QueryExecutor>
        queryExecutors, String defaultExecutor)
    {
        this.dslContext = dslContext;
        this.queryTransformer = queryTransformer;
        this.queryExecutors = queryExecutors;
        this.defaultExecutor = defaultExecutor;
    }


    @Override
    public Map<String, Object> provide(DataProviderContext dataProviderContext, ComponentModel componentModel, Map
        <String, Object> vars)
    {
        ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
        if (componentRegistration == null)
        {
            log.warn("No componentRegistration found for {}", componentModel);
            return null;
        }

        Map<String, QueryDefinition> runtimeQueries = prepare(dataProviderContext, componentRegistration, componentModel, vars);
        if (runtimeQueries == null)
        {
            return null;
        }

        Map<String, String> executors = componentRegistration.getDescriptor().getQueryExecutors();

        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, QueryDefinition> entry : runtimeQueries.entrySet())
        {
            String name = entry.getKey();
            QueryDefinition queryDefinition = entry.getValue();

            String nameFromRegistration = executors != null ? executors.get(name) : null;
            String beanName = nameFromRegistration != null ? nameFromRegistration : defaultExecutor;

            QueryExecutor executor = queryExecutors.get(beanName);
            if (executor == null)
            {
                throw new IllegalStateException("No query executor with name '" + beanName + "'");
            }
            Object result = executor.execute(queryDefinition);

            log.debug("Result: {}", result);

            map.put(name, result);
        }

        return map;

    }


    private Map<String, QueryDefinition> prepare(DataProviderContext dataProviderContext, ComponentRegistration registration, ComponentModel elem, Map<String,
        Object> vars)
    {
        Map<String, Object> queries = registration.getDescriptor().getQueries();
        if (queries == null)
        {
            return null;
        }

        Map<String, QueryDefinition> preparedQueries = new HashMap<>();
        for (Map.Entry<String, Object> entry : queries.entrySet())
        {
            String name = entry.getKey();
            Object value = entry.getValue();


            if (!(value instanceof String))
            {
                throw new QueryPreparationException("Queries for QueryDataProvider must be query expression strings" + registration + ", elem = " + elem);
            }

            String queryExpression = (String)value;
            try
            {
                DomainService domainService = dataProviderContext.getRuntimeContext().getRuntimeApplication()
                    .getDomainService();
                QueryDefinition definition = queryTransformer.transform(domainService, queryExpression, registration, elem, vars);
                preparedQueries.put(name, definition);
            }
            catch(Exception e)
            {
                throw new DataProviderPreparationException("Error parsing expression '" + name + "' = " + queryExpression + " of " + elem, e);
            }
            catch(TokenMgrError e)
            {
                throw new DataProviderPreparationException("Token error parsing expression '" + name + "' = " + queryExpression + " of " + elem, e);
            }

        }
        return preparedQueries;
    }
}
