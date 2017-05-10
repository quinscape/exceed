package de.quinscape.exceed.runtime.service.search;

/**
 * Detail information for a TRANSITION match
 */
public class TransitionResultDetail
{
    private final String stateName, transitionName;


    public TransitionResultDetail(String stateName, String transitionName)
    {
        this.stateName = stateName;
        this.transitionName = transitionName;
    }


    public String getStateName()
    {
        return stateName;
    }


    public String getTransitionName()
    {
        return transitionName;
    }
}
