package de.quinscape.exceed.model.component;

import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.DocumentedCollection;
import org.svenson.JSONTypeHint;

import java.util.Map;

/**
 * Contains component descriptions for all components in a component package directory. It is the result of parsing a
 * "components.json" file.
 * <p>
 * The package descriptor differs from the other top level models in that it is not subject to composition by the
 * ModelCompositionService but is handled by ComponentRegistryImpl.
 * </p>
 * <p>
 * It describes component
 * </p>
 */
public class ComponentPackageDescriptor
    extends AbstractTopLevelModel
{
    private Map<String, ComponentDescriptor> components;

    private String description;

    /**
     * Returns the a map mapping component names to the component descriptors for that component.
     *
     * @return components map
     */
    public Map<String, ComponentDescriptor> getComponents()
    {
        return components;
    }

    @JSONTypeHint(ComponentDescriptor.class)
    @DocumentedCollection(keyDesc = "componentName")
    public void setComponents(Map<String, ComponentDescriptor> components)
    {
        this.components = components;

        for (ComponentDescriptor componentDescriptor : components.values())
        {
            componentDescriptor.setPackageDescriptor(this);
            componentDescriptor.validate();
        }
    }


    /**
     * Description for the component package.
     * 
     * @return
     */
    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        throw new UnsupportedOperationException();
    }
}
