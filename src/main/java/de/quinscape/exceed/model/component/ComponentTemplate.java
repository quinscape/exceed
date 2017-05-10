package de.quinscape.exceed.model.component;

import de.quinscape.exceed.model.view.ComponentModel;
import org.svenson.JSONParameter;

/**
 * A template model to create components of a certain type within the code editor.
 * 
 */
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


    /**
     * Description of this template
     */
    public String getDescription()
    {
        return description;
    }


    /**
     * Component model for this template
     */
    public ComponentModel getComponentModel()
    {
        return componentModel;
    }


    /**
     * Wizard component for this template.
     */
    public String getWizard()
    {
        return wizard;
    }


    /**
     * Title for this template.
     */
    public String getTitle()
    {
        return title;
    }
}
