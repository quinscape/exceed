package de.quinscape.exceed.model.config;

import org.svenson.JSONProperty;

/**
 * Contains component configurations for the components within an exceed applications. Is embedded in
 * {@link ApplicationConfig}
 * <p>
 * This is were app-level configuration for components is stored. Is provided
 * </p>
 */
public class ComponentConfig
{
    /**
     * Whether input fields should propagate their values immediately as the user types to the context scopes etc or
     * if it should only validate the field but wait with propagating the value until the user leaves the field.
     * <p>
     *     Default is <code>true</code>
     * </p>
     */
    private boolean instantTextFields = true;

    private BaseTemplateConfig baseTemplateConfig;

    public boolean isInstantTextFields()
    {
        return instantTextFields;
    }


    public void setInstantTextFields(boolean instantTextFields)
    {
        this.instantTextFields = instantTextFields;
    }

    @JSONProperty("baseTemplate")
    public BaseTemplateConfig getBaseTemplateConfig()
    {
        return baseTemplateConfig;
    }


    public void setBaseTemplateConfig(BaseTemplateConfig baseTemplateConfig)
    {
        this.baseTemplateConfig = baseTemplateConfig;
    }
}
