package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.runtime.RuntimeContext;

public interface ParameterProvider
{
    /**
     * Provides the parameter value for the given runtime context.
     *
     * @param runtimeContext        runtime context
     * @return value
     */
    Object provide(RuntimeContext runtimeContext);
}
