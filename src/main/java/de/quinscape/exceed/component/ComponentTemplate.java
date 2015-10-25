package de.quinscape.exceed.component;

import de.quinscape.exceed.model.view.ComponentModel;

public class ComponentTemplate
{
    private final String name;

    private final ComponentModel componentModel;


    public ComponentTemplate(String name, ComponentModel componentModel)
    {
        this.name = name;
        this.componentModel = componentModel;
    }


    public String getName()
    {
        return name;
    }


    public ComponentModel getComponentModel()
    {
        return componentModel;
    }
}
