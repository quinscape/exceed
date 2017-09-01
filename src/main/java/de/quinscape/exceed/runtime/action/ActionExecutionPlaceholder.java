package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;
/**
 * Singleton that acts as a placeholder provider for {@link ActionExecution} parameters.
 *
 * The actual providing is done as special case in {@link ParameterInfo#provide(RuntimeContext, List, ActionExecution)}
 */
public final class ActionExecutionPlaceholder
    implements ParameterProvider
{


    public final static ActionExecutionPlaceholder INSTANCE = new ActionExecutionPlaceholder();

    private ActionExecutionPlaceholder()
    {
    }

    @Override
    public Object provide(RuntimeContext runtimeContext)
    {
        throw new UnsupportedOperationException();
    }
}
