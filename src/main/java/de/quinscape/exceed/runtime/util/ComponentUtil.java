package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;

import java.util.List;
import java.util.Set;

public class ComponentUtil
{

    /**
     * Updates the application component registration for every component in the give view.
     * It also makes sure that every component model knows its parent.
     *
     * @param view               component model
     * @param componentNames     component names to update or <code>null</code> or empty to update all components.
     */
    public static void updateComponentRegsAndParents(ComponentRegistry componentRegistry, View view, Set<String>
        componentNames)
    {
        if (componentNames != null && componentNames.size() == 0)
        {
            componentNames = null;
        }
        updateComponentRegsAndParentsRecursive(componentRegistry, view.getRoot(), componentNames, null);
    }


    private static void updateComponentRegsAndParentsRecursive(ComponentRegistry componentRegistry, ComponentModel elem, Set<String> componentNames, ComponentModel parent)
    {
        if (elem.isComponent() && (componentNames == null || componentNames.contains(elem.getName())))
        {
            ComponentRegistration registration = componentRegistry.getComponentRegistration(elem.getName());
            if (registration != null)
            {
                final String componentId = elem.getComponentId();

                if (RuntimeApplication.RUNTIME_INFO_NAME.equals(componentId))
                {
                    throw new IllegalStateException("'" + RuntimeApplication.RUNTIME_INFO_NAME + "' is a reserved component name.");
                }

                if (registration.getDataProvider() != null && componentId == null)
                {
                    final String id = COUNTER.nextId();
                    setIdAttribute(elem, id);
                }

                elem.setComponentRegistration(registration);
            }

            elem.setParent(parent);
        }

        for (ComponentModel componentModel : elem.children())
        {
            updateComponentRegsAndParentsRecursive(componentRegistry, componentModel, componentNames, elem);
        }
    }


    private static void setIdAttribute(ComponentModel elem, String id)
    {
        final Attributes attrs = elem.getAttrs();
        if (attrs == null)
        {
            final Attributes newAttrs = new Attributes();
            newAttrs.setAttribute("id", id);
            elem.setAttrs(newAttrs);
        }
        else
        {
            attrs.setAttribute("id", id);
        }
    }


    private final static IdCounter COUNTER = new IdCounter("id-");

    public static int findFlatIndex(View view, String componentId)
    {
        FindFlatResult res = new FindFlatResult();
        findFlatIndexRecursive(view.getRoot(), componentId, res);
        return res.index;
    }


    private static boolean findFlatIndexRecursive(ComponentModel model, String componentId, FindFlatResult res)
    {
        if (componentId.equals(model.getComponentId()))
        {
            return true;
        }

        res.index++;

        List<ComponentModel> kids = model.getKids();
        if (kids != null)
        {
            for (ComponentModel kid : kids)
            {
                if (findFlatIndexRecursive(kid, componentId, res))
                {
                    return true;
                }
            }
        }
        return false;
    }


    private static class FindFlatResult
    {
        private int index = 0;
    }
}
