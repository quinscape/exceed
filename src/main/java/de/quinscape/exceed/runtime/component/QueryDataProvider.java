package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.expression.TokenMgrError;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.expression.query.InvalidFieldReferenceException;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.view.DataProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Data provider implementation for the default query definition mechanism. Can be replaced
 * by setting an alternate DataProvider in {@link ComponentDescriptor#dataProvider}
 */
public class QueryDataProvider
    implements DataProvider
{
    private final static Logger log = LoggerFactory.getLogger(QueryDataProvider.class);

    private final QueryTransformer queryTransformer;

    private final StorageConfigurationRepository storageConfigurationRepository;


    public QueryDataProvider(QueryTransformer queryTransformer, StorageConfigurationRepository storageConfigurationRepository)
    {
        this.queryTransformer = queryTransformer;
        this.storageConfigurationRepository = storageConfigurationRepository;
    }


    @Override
    public Map<String, Object> provide(
        DataProviderContext dataProviderContext,
        ComponentModel componentModel,
        Map<String, Object> vars
    )
    {
        ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
        if (componentRegistration == null)
        {
            log.warn("No componentRegistration found for {}", componentModel);
            return null;
        }

        PreparedQueries queries = prepare(dataProviderContext, componentModel, vars);
        if (queries == null)
        {
            return null;
        }


        Map<String, QueryDefinition> runtimeQueries = queries.getQueryDefinitions();
        registerQueryTranslations(dataProviderContext, runtimeQueries);

        Map<String, Object> map = new HashMap<>();


        for (Map.Entry<String, QueryDefinition> entry : runtimeQueries.entrySet())
        {
            String name = entry.getKey();
            QueryDefinition queryDefinition = entry.getValue();
            final String config = queryDefinition.getQueryDomainType().getType().getStorageConfiguration();
            final StorageConfiguration configuration = storageConfigurationRepository.getConfiguration(config);
            QueryExecutor executor = configuration.getQueryExecutor();

            if (executor == null)
            {
                throw new UnsupportedOperationException("Querying not supported by storage config '" + config + "'");
            }

            Object result = executor.execute(dataProviderContext.getRuntimeContext(), queryDefinition);
            log.debug("Result: {}", result);
            map.put(name, result);
        }

        map.putAll(queries.getQueryErrors());

        return map;
    }


    /**
     * Registers the translations used in the map of runtime queries. Calls {@link #registerTranslations()} which can
     * be overridden for custom translation keys.
     *
     * @param dataProviderContext
     * @param runtimeQueries
     */
    private void registerQueryTranslations(DataProviderContext dataProviderContext, Map<String, QueryDefinition> runtimeQueries)
    {
        for (QueryDefinition definition : runtimeQueries.values())
        {
            QueryDomainType current = definition.getQueryDomainType();

            while(current != null)
            {
                dataProviderContext.registerTranslations(current.getType());
                current = current.getJoinedWith();
            }
        }

        this.registerTranslations();
    }


    /**
     * Can be overridden to provide custom translations for a query driven component.
     */
    protected void registerTranslations()
    {
        // intentionally empty
    }

    private PreparedQueries prepare(
        DataProviderContext dataProviderContext,
        ComponentModel elem,
        Map<String,Object> vars
    )
    {
        final PreparedQueries preparedQueries = new PreparedQueries();
        ComponentRegistration registration = elem.getComponentRegistration();
        Map<String, Object> queries = registration.getDescriptor().getQueries();
        if (queries == null)
        {
            return null;
        }

        for (Map.Entry<String, Object> entry : queries.entrySet())
        {
            String name = entry.getKey();

            if (elem.getAttribute(name) == null)
            {
                Object value = entry.getValue();

                if (!(value instanceof String))
                {
                    throw new QueryPreparationException("Queries for QueryDataProvider must be query expression " +
                        "strings" + registration + ", elem = " + elem);
                }

                String queryExpression = (String) value;
                try
                {
                    QueryDefinition definition = queryTransformer.transform(dataProviderContext.getRuntimeContext(),
                        queryExpression, elem, vars);

                    preparedQueries.addDefinition(name, definition);
                }
                catch(InvalidFieldReferenceException e)
                {
                    preparedQueries.addError(name, new QueryError(name ,"Invalid field reference in query", e));
                }
                catch (Exception e)
                {
                    preparedQueries.addError(name, new QueryError(name,"Error parsing expression '" +
                        name + "' = " + queryExpression + " of " + elem, e));
                }
                catch (TokenMgrError e)
                {
                    preparedQueries.addError(name, new QueryError(name,"Token error parsing expression" +
                        " '" + name + "' = " + queryExpression + " of " + elem, e));
                }
            }
        }
        return preparedQueries;
    }
}
