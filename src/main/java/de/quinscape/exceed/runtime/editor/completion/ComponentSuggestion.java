package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.model.view.ComponentModel;

/**
 * Created by sven on 11.02.16.
 */
public class ComponentSuggestion
{
    private final String componentName;

    private final String title;

    private final String description;

    private final ComponentModel componentModel;

    private final int wizardIndex;


    public ComponentSuggestion(String componentName, String title, String description, ComponentModel componentModel, int wizardIndex)
    {
        boolean isComponent = componentModel != null;
        boolean isWizard = wizardIndex >= 0;

        if (isComponent == isWizard)
        {
            throw new IllegalArgumentException("Need either a component model or a wizard index");
        }

        this.componentName = componentName;
        this.title = title;
        this.description = description;
        this.componentModel = componentModel;
        this.wizardIndex = wizardIndex;
    }


    public String getComponentName()
    {
        return componentName;
    }


    public String getDescription()
    {
        return description;
    }


    public ComponentModel getComponentModel()
    {
        return componentModel;
    }


    public int getWizardIndex()
    {
        return wizardIndex;
    }


    public String getTitle()
    {
        return title;
    }
}
