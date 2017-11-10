package de.quinscape.exceed.runtime.universal;

import de.quinscape.exceed.runtime.RuntimeContext;

public interface ServerRenderingStrategy
{
    /**
     * Returns <code>true</code> if the server should pre-render the
     * initial view on the server side.
     *
     * @param runtimeContext    runtime context
     *
     * @return <code>true</code> if pre-rendering.
     */
    boolean shouldDoServerSideRendering(RuntimeContext runtimeContext);
}
