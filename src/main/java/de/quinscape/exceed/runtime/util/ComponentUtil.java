package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.model.ModelCompositionException;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;

import java.util.Set;

public class ComponentUtil
{

    /**
     * Updates the application component registration for the given component model and its children
     *
     * @param elem               component model
     * @param componentNames     component names to update or <code>null</code> or empty to update all components.
     */
    public static void updateComponentRegistrations(ComponentRegistry componentRegistry, ComponentModel elem, Set<String>
        componentNames)
    {
        if (componentNames != null && componentNames.size() == 0)
        {
            componentNames = null;
        }
        updateComponentRegistrationsRecursive(componentRegistry, elem, componentNames);
    }


    private static void updateComponentRegistrationsRecursive(ComponentRegistry componentRegistry, ComponentModel elem, Set<String> componentNames)
    {
        if (elem.isComponent() && (componentNames == null || componentNames.contains(elem.getName())))
        {
            ComponentRegistration registration = componentRegistry.getComponentRegistration(elem.getName());

            if (registration == null)
            {
                throw new IllegalStateException("No component registered for name '" + elem.getName() + "'");
            }

            if (registration.getDataProvider() != null && elem.getComponentId() == null)
            {
                throw new ModelCompositionException(elem + " must have a id attribute");
            }
            elem.setComponentRegistration(registration);
        }

        for (ComponentModel componentModel : elem.children())
        {
            updateComponentRegistrationsRecursive(componentRegistry, componentModel, componentNames);
        }
    }

}
