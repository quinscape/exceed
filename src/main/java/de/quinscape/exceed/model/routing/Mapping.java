package de.quinscape.exceed.model.routing;


import de.quinscape.exceed.model.Model;
import org.svenson.JSONProperty;

public class Mapping
    extends Model
{
    private String alias;

    private String viewName;

    private String processName;


    public String getViewName()
    {
        return viewName;
    }


    public void setViewName(String viewName)
    {
        this.viewName = viewName;
    }


    public String getAlias()
    {
        return alias;
    }


    public void setAlias(String alias)
    {
        this.alias = alias;
    }


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
        if (alias != null)
        {
            return alias;
        }
        if (viewName != null)
        {
            return viewName;
        }
        return processName;
    }
}
