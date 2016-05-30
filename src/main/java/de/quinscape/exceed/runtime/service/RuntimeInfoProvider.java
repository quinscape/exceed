package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.RuntimeContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Implemented by classes that want to provide runtime information to the js view. All spring beans implementing this
 * interface will be automatically used.
 */
public interface RuntimeInfoProvider
{
    /**
     * Name under which the view will access the runtime information. Must be unique.
     *
     * @return  unique name
     */
    String getName();

    /**
     * Is called on every request to provide a JSONable runtime information object
     *
     * @param request           current HTTP request
     * @param runtimeContext    current runtime context
     * @return
     */
    Object provide(HttpServletRequest request, RuntimeContext runtimeContext) throws Exception;
}
