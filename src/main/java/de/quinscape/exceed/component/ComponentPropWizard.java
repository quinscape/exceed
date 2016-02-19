package de.quinscape.exceed.component;

import org.svenson.JSONParameter;

public class ComponentPropWizard
{
    private final String wizard;

    private final String title;

    private final String description;


    public ComponentPropWizard(
        @JSONParameter("wizard")
        String wizard,
        @JSONParameter("title")
        String title,
        @JSONParameter("description")
        String description)
    {
        this.wizard = wizard;
        this.title = title;
        this.description = description;
    }


    public String getWizard()
    {
        return wizard;
    }


    public String getTitle()
    {
        return title;
    }


    public String getDescription()
    {
        return description;
    }
}
