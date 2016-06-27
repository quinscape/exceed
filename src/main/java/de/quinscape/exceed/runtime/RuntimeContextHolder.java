package de.quinscape.exceed.runtime;

/**
 * Provides access to the {@link RuntimeContext} associated with the current Thread.
 *
 */
public class RuntimeContextHolder
{
    private final static ThreadLocal<RuntimeContext> runtimeContextThreadLocal = new ThreadLocal<>();

    /**
     * Returns the current RuntimeContext for the current Thread.
     * @return  runtime context
     */
    public static RuntimeContext get()
    {
        return runtimeContextThreadLocal.get();
    }


    /**
     * Registers the given runtime context for the current Thread.
     *
     * Normally only the system uses this method to publish the newly created runtime context
     *
     * @see de.quinscape.exceed.runtime.controller.ApplicationController
     * @see de.quinscape.exceed.runtime.controller.ActionController
     *
     * @param runtimeContext runtime context.
     */
    public static void register(RuntimeContext runtimeContext)
    {
        runtimeContextThreadLocal.set(runtimeContext);
    }


    /**
     * Removes the runtime conext for the current Thread.
     */
    public static void clear()
    {
        register(null);
    }
}
