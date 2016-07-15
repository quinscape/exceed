package de.quinscape.exceed.runtime.component.translation;

/**
 * Created by sven on 05.07.16.
 */
public class RuleLocation
{
    private final String processName;

    private final String viewName;


    public RuleLocation(String processName, String viewName)
    {
        if (viewName == null && processName == null)
        {
            throw new IllegalStateException("Invalid rule location: Process and view reference can't both be null.");
        }

        this.processName = processName;
        this.viewName = viewName;
    }


    public String getProcessName()
    {
        return processName;
    }


    public String getViewName()
    {
        return viewName;
    }
}
