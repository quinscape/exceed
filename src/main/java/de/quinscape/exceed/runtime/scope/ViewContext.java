package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.Map;

public class ViewContext
    extends AbstractScopedContext
{
    private final String viewName;


    public ViewContext(ContextModel contextModel, String viewName)
    {
        super(contextModel);

        this.viewName = viewName;
    }

    protected ViewContext(ContextModel contextModel, Map<String, Object> context, String viewName)
    {
        super(contextModel, context);

        this.viewName = viewName;
    }


    @Override
    public ScopedContext copy(RuntimeContext runtimeContext)
    {
        return new ViewContext(getContextModel(), context, viewName);
    }


    public Map<String, Object> getContext()
    {
        return context;
    }
}
