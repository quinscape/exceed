package de.quinscape.exceed.model.component;

import de.quinscape.exceed.model.view.ComponentModel;
import org.svenson.JSONParameter;

public class ComponentTemplate
{
    private final String description;

    private final ComponentModel componentModel;
    private final String wizard;

    private final String title;


    public ComponentTemplate(

        @JSONParameter("desc")
        String description,
        @JSONParameter("title")
        String title,
        @JSONParameter("model")
        ComponentModel componentModel,
        @JSONParameter("wizard")
        String wizard
    )
    {
        this.description = description;
        this.title = title;
        this.componentModel = componentModel;
        this.wizard = wizard;
    }

    public String getDescription()
    {
        return description;
    }


    public ComponentModel getComponentModel()
    {
        return componentModel;
    }


    public String getWizard()
    {
        return wizard;
    }


    public String getTitle()
    {
        return title;
    }
}
