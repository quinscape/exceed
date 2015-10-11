package de.quinscape.exceed.component;

import org.svenson.JSONTypeHint;

import java.util.Map;

/**
 * Encapsulates a component package descriptor which is the result of parsing a "component.json" file.
 *
 */
public class ComponentPackageDescriptor
{
    private Map<String, ComponentDescriptor> components;


    /**
     * Returns the a map mapping component names to the component descriptors for that component.
     *
     * @return components map
     */
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
