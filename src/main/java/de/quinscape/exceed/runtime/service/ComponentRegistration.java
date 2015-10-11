package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.component.ComponentDescriptor;

/**
 * Encapsulates the component descriptor and the a reference to a potential style sheet for a single component
 * within a component package.
 */
public class ComponentRegistration
{
    private final ComponentDescriptor descriptor;

    private final String styleSheetName;

    private final String styles;

    private final String componentName;


    ComponentRegistration(String componentName, ComponentDescriptor descriptor, String styleSheetName, String styles)
    {
        this.componentName = componentName;
        this.descriptor = descriptor;
        this.styleSheetName = styleSheetName;
        this.styles = styles;
    }


    public ComponentDescriptor getDescriptor()
    {
        return descriptor;
    }


    public String getStyleSheetName()
    {
        return styleSheetName;
    }


    public String getStyles()
    {
        return styles;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "componentName = '" + componentName + '\''
            + ", styleSheetName = '" + styleSheetName + '\''
            ;
    }
}
