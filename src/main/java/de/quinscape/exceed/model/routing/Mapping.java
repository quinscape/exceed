package de.quinscape.exceed.model.routing;


import de.quinscape.exceed.model.annotation.MergeStrategy;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.merge.ModelMergeMode;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.svenson.JSONProperty;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * A single routing table mapping.
 */
@MergeStrategy(ModelMergeMode.REPLACE)
public class Mapping
{
    private String viewName;

    private String processName;

    private Boolean disabled;

    private Set<String> classes;

    private ExpressionValue title;

    public Mapping()
    {
        this(null, null);
    }

    public Mapping(
        String viewName, String processName
    )
    {
        this.viewName = viewName;
        this.processName = processName;
    }


    public String getTitle()
    {
        return title != null ? title.getValue() : null;
    }


    public void setTitle(String title)
    {
        this.title = ExpressionValue.forValue(title, true);
    }


    @JSONProperty(ignore = true)
    public ExpressionValue getTitleValue()
    {
        return title;
    }


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


    public Set<String> getClasses()
    {
        return classes;
    }


    public void setClasses(Set<String> classes)
    {
        this.classes = classes;
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
