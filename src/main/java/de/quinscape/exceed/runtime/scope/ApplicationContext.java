package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Application scoped context implementation.
 */
public final class ApplicationContext
    extends AbstractChangeTrackingScopedContext
{
    private String name;


    public ApplicationContext(ContextModel contextModel)
    {
        super(contextModel);
    }

    protected ApplicationContext(ContextModel contextModel, Map<String,Object> context)
    {
        super(contextModel, context);
    }

    @Override
    public ScopedContext copy(RuntimeContext runtimeContext)
    {
        return new ApplicationContext(getContextModel(), new HashMap<>(context));
    }


    public Map<String, Object> getContextMap()
    {
        return context;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }
}
