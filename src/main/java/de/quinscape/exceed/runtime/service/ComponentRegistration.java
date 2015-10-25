package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.runtime.component.DataProvider;

import java.util.Map;

/**
 * Encapsulates the registration of a component definition and contains the component descriptor, the processed styles
 * for the component and the data provider implementation.
 */
public class ComponentRegistration
{
    private final ComponentDescriptor descriptor;

    private final String styles;

    private final String componentName;

    private final DataProvider dataProvider;


    ComponentRegistration(String componentName, ComponentDescriptor descriptor, String styles,
                          DataProvider dataProvider)
    {
        if (componentName == null)
        {
            throw new IllegalArgumentException("componentName can't be null");
        }

        if (descriptor == null)
        {
            throw new IllegalArgumentException("descriptor can't be null");
        }

        this.componentName = componentName;
        this.descriptor = descriptor;
        this.styles = styles;
        this.dataProvider = dataProvider;
    }


    public ComponentDescriptor getDescriptor()
    {
        return descriptor;
    }


    public String getComponentName()
    {
        return componentName;
    }


    public String getStyles()
    {
        return styles;
    }


    public DataProvider getDataProvider()
    {
        return dataProvider;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "componentName = '" + componentName + '\''
            + ", dataProvider = " + dataProvider
            ;
    }

}
