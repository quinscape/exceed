package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Session scoped context implementation.
 */
public final class SessionContext
    extends AbstractChangeTrackingScopedContext
{
    public SessionContext(ContextModel contextModel)
    {
        super(contextModel);
    }

    protected SessionContext(ContextModel contextModel, Map<String,Object> context)
    {
        super(contextModel, context);
    }

    @Override
    public ScopedContext copy(RuntimeContext runtimeContext)
    {
        return new SessionContext(getContextModel(), new HashMap<>(context));
    }
}
