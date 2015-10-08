package de.quinscape.exceed.runtime.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewData
{
    private final String viewName;

    private final Map<String,Object> componentData;

    public ViewData(String viewName)
    {
        this.viewName = viewName;
        this.componentData = new HashMap<>();
    }

    public String getViewName()
    {
        return viewName;
    }

    public Map<String, Object> getComponentData()
    {
        return componentData;
    }
}
