package de.quinscape.exceed.runtime;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides access to the {@link RuntimeContext} associated with the current Thread.
 *
 */
public class RuntimeContextHolder
{
    private final static ThreadLocal<RuntimeContext> runtimeContextThreadLocal = new ThreadLocal<>();

    private static final String RUNTIME_CONTEXT_REQUEST_ATTRIBUTE = RuntimeContextHolder.class.getName() + ":runtimeContext";


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
     *@param runtimeContext runtime context.
     * @param request
     */
    public static void register(RuntimeContext runtimeContext, HttpServletRequest request)
    {
        runtimeContextThreadLocal.set(runtimeContext);
        if (request != null)
        {
            request.setAttribute(RUNTIME_CONTEXT_REQUEST_ATTRIBUTE, runtimeContext);
        }
    }


    /**
     * Removes the runtime conext for the current Thread.
     */
    public static void clear()
    {
        register(null, null);
    }


    public static RuntimeContext get(HttpServletRequest request)
    {
        return (RuntimeContext) request.getAttribute(RUNTIME_CONTEXT_REQUEST_ATTRIBUTE);
    }
}
