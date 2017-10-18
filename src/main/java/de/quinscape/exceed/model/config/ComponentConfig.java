package de.quinscape.exceed.model.config;

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


    public boolean isInstantTextFields()
    {
        return instantTextFields;
    }


    public void setInstantTextFields(boolean instantTextFields)
    {
        this.instantTextFields = instantTextFields;
    }
}
