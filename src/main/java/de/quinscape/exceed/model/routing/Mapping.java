package de.quinscape.exceed.model.routing;


import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.svenson.JSONProperty;

import javax.annotation.PostConstruct;

/**
 * A single routing table mapping.
 */
public class Mapping
{
    private String viewName;

    private String processName;

    private Boolean disabled;


    /**
     * <code>true</code> if the mapping is currently disabled.
     * @return
     */
    @JSONProperty(ignoreIfNull = true)
    public Boolean getDisabled()
    {
        return disabled;
    }


    public void setDisabled(Boolean disabled)
    {
        this.disabled = disabled;
    }


    /**
     * The view name mapped to this location. Mutually exclusive with <code>processName</code>.
     */
    public String getViewName()
    {
        return viewName;
    }


    public void setViewName(String viewName)
    {
        this.viewName = viewName;
    }


    /**
     * The process name mapped to this location. Mutually exclusive with <code>viewName</code>.
     */
    public String getProcessName()
    {
        return processName;
    }


    public void setProcessName(String processName)
    {
        this.processName = processName;
    }


    @JSONProperty(ignore = true)
    public String getName()
    {
        if (viewName != null)
        {
            return viewName;
        }
        return processName;
    }


    @PostConstruct
    public void validate()
    {
        if (viewName == null && processName == null)
        {
            throw new InconsistentModelException("Mapping must have either a viewName or processName property");
        }
        if (viewName != null && processName != null)
        {
            throw new InconsistentModelException("Mapping cannot have both viewName and processName");
        }
    }
}
