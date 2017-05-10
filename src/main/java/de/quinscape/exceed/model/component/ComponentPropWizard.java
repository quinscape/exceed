package de.quinscape.exceed.model.component;

import org.svenson.JSONParameter;

/**
 * A wizard component definition for the component.
 *
 */
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


    /**
     * Wizard Component js name.
     * @return
     */
    public String getWizard()
    {
        return wizard;
    }


    /**
     * Title for the prop wizard component
     * @return
     */
    public String getTitle()
    {
        return title;
    }


    /**
     * Description for the prop wizard
     * @return
     */
    public String getDescription()
    {
        return description;
    }
}
