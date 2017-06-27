package de.quinscape.exceed.model.translation;

/**
 * Encapsulates a location to possibly have a local translation rule.
 */
public class TranslationRuleLocation
{
    private final String processName;

    private final String viewName;


    /**
     * Creates a new location object.
     *
     * @param processName   process name of the location. Might be <code>null</code> for standalone views.
     * @param viewName      view name of the location. Might be <code>null</code> to express a process wide translation
     *                      change.
     * @throws IllegalStateException if both parameters are <code>null</code>
     */
    public TranslationRuleLocation(String processName, String viewName)
    {
        if (viewName == null && processName == null)
        {
            throw new IllegalStateException("Invalid rule location: Process and view reference can't both be null.");
        }

        this.processName = processName;
        this.viewName = viewName;
    }


    /**
     * Returns the process name of the location. Might be <code>null</code> for standalone views.
     *
     * @return
     */
    public String getProcessName()
    {
        return processName;
    }


    /**
     * Returns the view name of the location. Might be <code>null</code> to express a process wide translation change.
     *
     * @return
     */
    public String getViewName()
    {
        return viewName;
    }
}
