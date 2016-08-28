package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.service.ContextExpressionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Extended by concrete scoped context implementations. Contains the common logic which is most of it.
 */
public abstract class AbstractScopedContext
    implements ScopedContext
{
    private final static Logger log = LoggerFactory.getLogger(AbstractScopedContext.class);

    protected final Map<String, Object> context;

    private final ContextModel contextModel;

    private boolean initialized;


    public AbstractScopedContext(ContextModel contextModel)
    {
        this(contextModel, contextModel != null ? new HashMap<>() : null);
        this.initialized = false;
    }

    protected AbstractScopedContext(ContextModel contextModel, Map<String,Object> context)
    {
        this.contextModel = contextModel;
        this.context = context;

        this.initialized = true;
    }

    @Override
    public Object getProperty(String name)
    {
        if (!hasProperty(name))
        {
            throw new ScopeResolutionException("Context has not property '" + name + "'");
        }
        return context.get(name);
    }


    @Override
    public void setProperty(String name, Object value)
    {
        if (!hasProperty(name))
        {
            throw new ScopeResolutionException("Context has not property '" + name + "'");
        }
        context.put(name, value);
    }


    @Override
    public boolean hasProperty(String name)
    {
        ensureInitialized();
        return contextModel != null && contextModel.getProperties().containsKey(name);
    }


    @Override
    public ContextModel getContextModel()
    {
        return contextModel;
    }

    /**
     * Returns a copy of the scoped context with an independent context value map.
     *
     * @param runtimeContext    runtime context
     * @return  scoped context copy
     */
    @Override
    public abstract ScopedContext copy(RuntimeContext runtimeContext);

    @Override
    public void init(RuntimeContext runtimeContext, ExpressionService expressionService, ActionService actionService, Map<String,Object> inputValues)
    {
        initialized = true;

        if (contextModel != null)
        {
            initProperties(runtimeContext, expressionService, actionService, inputValues);
        }
    }

    private void initProperties(RuntimeContext runtimeContext, ExpressionService expressionService, ActionService
        actionService, Map<String, Object> inputValues)
    {
        DomainService domainService = runtimeContext.getDomainService();

        for (ScopedPropertyModel scopedPropertyModel : contextModel.getProperties().values())
        {
            String name = scopedPropertyModel.getName();

            Object value = null;
            if (inputValues != null)
            {
                Object input = inputValues.get(name);
                if (input != null)
                {
                    try
                    {
                        value = domainService.getPropertyConverter(scopedPropertyModel.getType()).convertToJava(runtimeContext, input);
                    }
                    catch(Exception e)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Error converting {} to '{}': {}", input , scopedPropertyModel.getType(), e);
                        }
                        // ignore error, use default
                        value = null;
                    }
                }
            }

            if (value == null)
            {
                ASTExpression defaultValueExpression = scopedPropertyModel.getDefaultValueExpression();
                if (defaultValueExpression != null)
                {
                    value = evaluateDefault(runtimeContext, expressionService, actionService, scopedPropertyModel,
                        defaultValueExpression);
                }
                else
                {
                    value = scopedPropertyModel.getDefaultValue();
                }
            }
            context.put(name, value);
        }
    }


    private Object evaluateDefault(RuntimeContext runtimeContext, ExpressionService expressionService, ActionService actionService, ScopedPropertyModel scopedPropertyModel, ASTExpression defaultValueExpression)
    {
        Object value;ContextExpressionEnvironment env = new ContextExpressionEnvironment(runtimeContext, actionService, runtimeContext.getLocationParams(), scopedPropertyModel);
        value = expressionService.evaluate(defaultValueExpression, env);
        return value;
    }


    private void ensureInitialized()
    {
        if (!initialized)
        {
            throw new IllegalStateException("Context " + this + " not initialized");
        }
    }


    @Override
    public boolean isInitialized()
    {
        return initialized;
    }

    public void update(Map<String, Object> contextUpdates)
    {
        if (contextUpdates == null || contextUpdates.size() == 0)
        {
            return;
        }

        for (String name : getContextModel().getProperties().keySet())
        {
            if (contextUpdates.containsKey(name))
            {
                context.put(name, contextUpdates.get(name));
            }
        }
    }


    @Override
    public ScopedPropertyModel getModel(String name)
    {
        return contextModel.getProperties().get(name);
    }
}
