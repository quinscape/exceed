package de.quinscape.exceed.runtime.service.client;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.view.ViewData;

import javax.servlet.http.HttpServletRequest;

/**
 * Implemented by classes that want to participate in providing an initial or updated state to the js view.
 *
 * <p>
 *     The implementations should be annotated with the appropriate annotation(s) to control in what contexts they appear
 * </p>
 *
 * @see ExceedAppProvider
 * @see ExceedEditorProvider
 */
public interface ClientStateProvider
{
    /**
     * Return the state property the client state result will have in the client side state. Must be unique.
     * <p>
     *     This name in the end determines the position of the data generated within the client side redux state.
     *     If {@link #isMutable()} returns false, a "meta." prefix is added to the state location.
     * </p>
     * <p>
     *     So a mutable client state provider with name <code>"foo"</code> would show up as <code>"state.foo"</code> in
     *     the redux state, if the provider is not mutable, it will be <code>state.meta.foo</code>.
     * </p>
     *
     * @return  unique name
     */
    String getName();

    /**
     * Returns the scope to use for the provided state part. Default is {@link ClientStateScope#REQUEST }
     *
     * <p>
     *     The scope controls how often the provider is actually called. A <code>VIEW</code> scoped provider will only
     *     be called once for every view, an <code>APPLICATION</code> scoped one once per application and so on.
     * </p>
     * <p>
     *     The  returned state values are stored in a cache. In development mode, they are flushed at the necessary times
     *     when views change etc.
     * </p>
     *
     * @return scope
     */
    ClientStateScope getScope();

    /**
     * Returns the client state result for the given request, runtime context and view data.
     *
     * @param request           current HTTP request
     * @param runtimeContext    current runtime context
     * @param viewData          view data provided for components
     *
     * @return ClientStateResult that can be turned to JSON with
     *
     * @see DefaultClientData
     * @see JSONData
     */
    ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws Exception;


    default boolean isSkippedOnAjax()
    {
        final ClientStateScope scope = getScope();
        return scope != ClientStateScope.REQUEST && scope != ClientStateScope.VIEW;
    }

    /**
     * Returns <code>true</code> if the data provided by this provider is mutable. Mutable data gets its own slice in the
     * redux state, immutable reference data gets moved to the simplified meta slice / reducer.
     * 
     * @return  <code>true</code> if provided data is mutable.
     */
    boolean isMutable();
}
