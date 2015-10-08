package de.quinscape.exceed.component;

import org.svenson.JSONTypeHint;

import java.util.Map;

public class ComponentPackage
{
    private Map<String, ComponentDescriptor> components;

    public Map<String, ComponentDescriptor> getComponents()
    {
        return components;
    }

    @JSONTypeHint(ComponentDescriptor.class)
    public void setComponents(Map<String, ComponentDescriptor> components)
    {
        this.components = components;
    }
}
