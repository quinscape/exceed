package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.ExpressionValueType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.domain.DomainOperations;
import de.quinscape.exceed.runtime.expression.query.QueryContext;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import de.quinscape.exceed.runtime.view.DataProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Data provider implementation for the default query definition mechanism. Can be replaced
 * by setting an alternate DataProvider in {@link ComponentDescriptor#dataProvider}
 */
public class QueryDataProvider
    implements DataProvider
{
    private final static Logger log = LoggerFactory.getLogger(QueryDataProvider.class);

    private final StorageConfigurationRepository storageConfigurationRepository;

    private final ActionService actionService;


    public QueryDataProvider(
        StorageConfigurationRepository storageConfigurationRepository,
        ActionService actionService
    )
    {
        this.storageConfigurationRepository = storageConfigurationRepository;
        this.actionService = actionService;
    }


    @Override
    public Map<String, Object> provide(
        DataProviderContext dataProviderContext,
        ComponentModel componentModel,
        Map<String, Object> vars
    )
    {
        ComponentInstanceRegistration componentRegistration = componentModel.getComponentRegistration();
        if (componentRegistration == null)
        {
            log.warn("No componentRegistration found for {}", componentModel);
            return null;
        }

        final RuntimeContext runtimeContext = dataProviderContext.getRuntimeContext();
        Map<String, ExpressionValue> queries = componentRegistration.getQueryExpressions(runtimeContext);
        if (queries == null)
        {
            return null;
        }


        final QueryTransformer queryTransformer = componentRegistration.getQueryTransformer();

        Map<String, Object> map = new HashMap<>();

        for (Map.Entry<String, ExpressionValue> e : queries.entrySet())
        {

            final String queryName = e.getKey();
            final ExpressionValue expressionValue = e.getValue();

            // we inject the query results into the props finally, so if we have a prop with that name already we
            // don't override it
            if (( componentModel.getAttribute(queryName) == null || !componentRegistration.getDescriptor().getPropTypes().get(queryName).isClient()) && expressionValue.getType() == ExpressionValueType.EXPRESSION)
            {

                long start = System.nanoTime();

                Function<QueryDefinition, Object> fn = (queryDefinition -> {

                    registerTranslations(dataProviderContext, queryDefinition);

                    final String config = queryDefinition.getQueryDomainType().getType().getStorageConfiguration();
                    final DomainOperations ops = storageConfigurationRepository.getConfiguration(config)
                        .getDomainOperations();

                    return ops.query(runtimeContext, runtimeContext.getDomainService(), queryDefinition);

                }
                );

                final Object transformed = queryTransformer.transform(
                    runtimeContext,
                    new QueryContext(
                        dataProviderContext.getRuntimeContext().getApplicationModel()
                            .getView(dataProviderContext.getViewName()),
                        componentModel,
                        vars,
                        fn,
                        actionService
                    ),
                    expressionValue.getAstExpression()
                );

                if (transformed instanceof QueryDefinition)
                {
                    final QueryDefinition queryDefinition = (QueryDefinition) transformed;
                    map.put(queryName, fn.apply(queryDefinition));
                }
                else
                {
                    map.put(queryName, transformed);
                }
            }
        }

        return map;
    }


    private void registerTranslations(
        DataProviderContext dataProviderContext, QueryDefinition queryDefinition
    )
    {
        if (queryDefinition != null)
        {
            QueryDomainType current = queryDefinition.getQueryDomainType();
            while (current != null)
            {
                dataProviderContext.registerTranslations(dataProviderContext.getRuntimeContext(), current.getType());

                current = QueryDomainType.nextJoinedType(current);
            }
        }
    }

}
