package de.quinscape.exceed.runtime.action.builtin;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.action.ActionEnvironment;
import de.quinscape.exceed.runtime.action.CustomLogic;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.expression.query.QueryContext;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.js.env.InspectUtil;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

@CustomLogic
public class ExceedBaseActions
{
    private final static Logger log = LoggerFactory.getLogger(ExceedBaseActions.class);

    private final QueryTransformer queryTransformer;
    private final StorageConfigurationRepository storageConfigurationRepository;


    @Autowired
    public ExceedBaseActions(
        QueryTransformer queryTransformer,
        StorageConfigurationRepository storageConfigurationRepository
    )
    {
        this.queryTransformer = queryTransformer;
        this.storageConfigurationRepository = storageConfigurationRepository;
    }


    @Action(
        description = "System log function. Server logs per slf4j, client to the console API"
    )
    public void syslog(Object... args)
    {
        if (log.isInfoEnabled())
        {
            StringBuilder sb = new StringBuilder();
            for (Object o : args)
            {

                sb.append(InspectUtil.inspect(o));
                sb.append(' ');
            }

            log.info(sb.toString());
        }
    }


    /**
     * Placeholder definition for client side navigateTo(). 
     * @param uri
     * @param params
     */
    @Action(
        description = "AJAX Navigation action within an exceed application. To be used in views without process.",
        env = ActionEnvironment.CLIENT
    )
    public void navigateTo(String uri, Map<String,Object> params)
    {
    }

    /**
     * Provides transition functionality as action to be able to construct more complex transition invocations (different
     * context, chaining etc).
     *
     * The client wrapper for this talks to the normal application controller and delegates the actual process state transition
     * handling to the process service.
     *
     * @see de.quinscape.exceed.runtime.service.ProcessService
     *
     * @param name      name of the transition
     */
    @Action(
        description = "An action that executes the given transition",
        env = ActionEnvironment.CLIENT
    )
    public void transition(String name)
    {
    }

    @Action(
        description = "An action that executes the given transition"
    )
    public void error(String message)
    {
        
    }


    /**
     * Alerts the user.
     * @param text  text to display
     */
    @Action(
        description = "Client-side bootstrap alert",
        env = ActionEnvironment.CLIENT
    )
    public void alert(String text)
    {
    }


    /**
     * Rereads the given object identity and merges the non-null properties of the given domain
     * object before returning it
     *
     * @param runtimeContext            runtime context
     * @param partialDomainObject       partial domain object
     *
     * @return completed domain object
     */
    @Action(
        description = "Merges the give domain object with its persistent state, i.e. it fills in all the missing null values"
    )
    public DomainObject merge(RuntimeContext runtimeContext, GenericDomainObject partialDomainObject)
    {
        return DomainUtil.merge(runtimeContext, partialDomainObject);
    }


    /**                                           A
     * Stores the given domain objects as-is. If it is a only partial domain object,
     * only that part will be stored. See {@link #merge(RuntimeContext, GenericDomainObject)}  for
     * merging such partial objects.
     *
     * @param runtimeContext    runtime context
     * @param domainObjects     domainObject
     */
    @Action(
        description = "Stores the given domain object"
    )
    public void store(RuntimeContext runtimeContext, GenericDomainObject... domainObjects)
    {
        for (GenericDomainObject domainObject : domainObjects)
        {
            domainObject.insertOrUpdate(runtimeContext);
        }
    }

    /**                                           A
     * Stores the given domain objects as-is. If it is a only partial domain object,
     * only that part will be stored. See {@link #merge(RuntimeContext, GenericDomainObject)}  for
     * merging such partial objects.
     *
     * @param runtimeContext    runtime context
     * @param domainObjects     domainObject
     */
    @Action(
        description = "Deletes the given domain objects"
    )
    public void delete(RuntimeContext runtimeContext, GenericDomainObject... domainObjects)
    {
        for (GenericDomainObject domainObject : domainObjects)
        {
            domainObject.delete(runtimeContext);
        }
    }

    private final static QueryContext QUERY_CONTEXT = new QueryContext(null, null, Collections.emptyMap(), null, null);

    @Action(
        description = "Query embedded in a server-side expression",
        env = ActionEnvironment.SERVER
    )
    public DataGraph query(RuntimeContext runtimeContext, ASTExpression astExpression)
    {
        if (queryTransformer == null)
        {
            throw new IllegalStateException("queryTransformer can't be null");
        }

        if (storageConfigurationRepository == null)
        {
            throw new IllegalStateException("storageConfigurationRepository can't be null");
        }

        final QueryDefinition queryDefinition = (QueryDefinition) queryTransformer.transform(runtimeContext, QUERY_CONTEXT, astExpression);
        final DomainType domainType = queryDefinition.getQueryDomainType().getType();
        final StorageConfiguration configuration = storageConfigurationRepository.getConfiguration(
            domainType.getStorageConfiguration());

        return configuration.getDomainOperations().query(runtimeContext,domainType.getDomainService(), queryDefinition);
    }

    @Action(
        description = "Action query embedded in a server-side expression",
        env = ActionEnvironment.SERVER
    )
    public DataGraph actionQuery(RuntimeContext runtimeContext, ASTExpression astExpression)
    {
        return query(runtimeContext, astExpression);
    }

}

