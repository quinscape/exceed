package de.quinscape.exceed.model.process;

/**
 * Represents an end state within a process.
 */
public class EndState
    extends ProcessState
{
    /**
     * Default view to show when toplevel process ends. This only happens in the case that a process that was was
     * invoked as sub-process invokes an end-state.
     */
    public static final String DEFAULT_END_VIEW = "ProcessEnd";

    private String viewName = DEFAULT_END_VIEW;


    public String getViewName()
    {
        return viewName;
    }


    public void setViewName(String viewName)
    {
        this.viewName = viewName;
    }
}
