package de.quinscape.exceed.model.component;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.DocumentedMapKey;
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
    extends TopLevelModel
{
    private Map<String, ComponentDescriptor> components;

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
    @DocumentedMapKey("componentName")
    public void setComponents(Map<String, ComponentDescriptor> components)
    {
        this.components = components;
    }


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        throw new UnsupportedOperationException();
    }
}
