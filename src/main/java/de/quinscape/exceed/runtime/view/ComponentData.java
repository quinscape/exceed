package de.quinscape.exceed.runtime.view;

import java.util.Map;

public class ComponentData
{
    private final Object vars;

    private final Object data;


    public ComponentData(Object vars, Object data)
    {
        this.vars = vars;
        this.data = data;
    }

    public Object getData()
    {
        return data;
    }

    public Object getVars()
    {
        return vars;
    }

}
