package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
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

    private DomainService domainService;


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
        ensureInitialized();

        final ScopedPropertyModel model = getModel(name);
        if (model.isRequired() && value == null)
        {
            throw new IllegalArgumentException("Cannot set required property '" + name + "' to null");
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
    public void init(RuntimeContext runtimeContext, JsEnvironment jsEnvironment, Map<String,Object> inputValues)
    {
        initialized = true;

        this.domainService = runtimeContext.getDomainService();

        if (contextModel != null)
        {
            initProperties(runtimeContext, jsEnvironment, inputValues);
        }
    }

    private void initProperties(RuntimeContext runtimeContext, JsEnvironment jsEnvironment, Map<String, Object> inputValues)
    {
        if (runtimeContext == null)
        {
            throw new IllegalArgumentException("runtimeContext can't be null");
        }

        if (jsEnvironment == null)
        {
            throw new IllegalArgumentException("jsEnvironment can't be null");
        }


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
                        value = scopedPropertyModel.getPropertyType().convertToJava(runtimeContext, input);
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
                    try
                    {
                        value = evaluateDefault(
                            runtimeContext,
                            jsEnvironment,
                            defaultValueExpression
                        );
                    }
                    catch (Exception e)
                    {
                        throw new IllegalStateException("Error evaluation default value: " + ExpressionRenderer.render(defaultValueExpression), e);
                    }
                }
                else
                {
                    value = scopedPropertyModel.getDefaultValue();
                }
            }
            context.put(name, value);
        }
    }


    private Object evaluateDefault(RuntimeContext runtimeContext, JsEnvironment jsEnvironment, ASTExpression defaultValueExpression)
    {
        return jsEnvironment.getValue(runtimeContext, defaultValueExpression);
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

    @Override
    public ScopedPropertyModel getModel(String name)
    {
        final ScopedPropertyModel model = contextModel.getProperties().get(name);
        if (model == null)
        {
            throw new ScopeResolutionException("Context has not property '" + name + "'");
        }
        return model;
    }



}
