package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.component.ComponentDescriptor;

public class ComponentRegistration
{
    private final ComponentDescriptor descriptor;
    private final String styles;

    ComponentRegistration(ComponentDescriptor descriptor, String styles)
    {
        this.descriptor = descriptor;
        this.styles = styles;
    }

    public ComponentDescriptor getDescriptor()
    {
        return descriptor;
    }

    public String getStyles()
    {
        return styles;
    }
}
