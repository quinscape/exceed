package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import org.svenson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * Application scoped context implementation.
 */
public final class ApplicationContext
    extends AbstractChangeTrackingScopedContext
{
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

    public String toJSON()
    {
        return JSON.defaultJSON().forValue(context);
    }
}
