package de.quinscape.exceed.runtime.service.client.scope;

import java.util.Set;

public class TransitionViewScopeReference
{
    private final Set<String> viewScopeRefs;
    private final boolean viewContextOnly;


    public TransitionViewScopeReference(Set<String> viewScopeRefs, boolean viewContextOnly)
    {
        this.viewScopeRefs = viewScopeRefs;
        this.viewContextOnly = viewContextOnly;
    }


    /**
     * Returns the set of view context values referenced in the transition.
     *
     * @return
     */
    public Set<String> getViewScopeRefs()
    {
        return viewScopeRefs;
    }


    /**
     * Returns <code>true</code>, if the transition is only setting view-context scope values and nothing else, i.e. if it can be
     * executed on the client side, synchronizing view-context values later.
     * @return
     */
    public boolean isViewContextOnly()
    {
        return viewContextOnly;
    }
}
