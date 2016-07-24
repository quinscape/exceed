package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ViewData
{
    private final String viewName;

    private final Map<String, Object> componentData;


    public ViewData(RuntimeContext runtimeContext, String viewName)
    {
        this.viewName = viewName;
        this.componentData = new HashMap<>();
    }


    public String getViewName()
    {
        return viewName;
    }


    public void provide(String componentId, Object vars, Object o)
    {
        componentData.put(componentId, new ComponentData(vars, o));
    }


    public Map<String, Object> getData()
    {
        return componentData;
    }
}
