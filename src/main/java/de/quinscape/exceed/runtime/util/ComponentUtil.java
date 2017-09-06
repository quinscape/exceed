package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.model.component.ComponentClasses;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.component.ComponentRegistration;
import de.quinscape.exceed.runtime.service.ComponentRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

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

        // provide parents for content subtrees
        for (ComponentModel componentModel : view.getContent().values())
        {
            updateComponentRegsAndParentsRecursive(componentRegistry, componentModel, componentNames, null);
        }

        final ComponentModel layoutRoot = view.getContent(View.ROOT);

        for (Map.Entry<String, ComponentModel> entry : view.getContent().entrySet())
        {
            final ComponentModel componentModel = entry.getValue();
            final String name = entry.getKey();

            final ComponentModel importingComponent = layoutRoot.find(c ->
            {
                if (!c.getName().equals(LayoutModel.CONTENT))
                    return false;

                final ExpressionValue attr = c.getAttribute("name");
                if (attr == null)
                {
                    return name.equals(View.MAIN);
                }
                else
                {
                    return attr.getValue().equals(name);
                }
            });

            if (importingComponent != null)
            {
                // set content root parent to the (first) importing component
                componentModel.setParent(importingComponent);
            }
        }
    }

    private static void updateComponentRegsAndParentsRecursive(
        ComponentRegistry componentRegistry,
        ComponentModel elem,
        Set<String> componentNames,
        ComponentModel parent
    )
    {
        if (elem.isComponent() && (componentNames == null || componentNames.contains(elem.getName())))
        {
            ComponentRegistration registration = componentRegistry.getComponentRegistration(elem.getName());
            if (registration != null)
            {
                final ExpressionValue idAttr = elem.getAttribute(ComponentModel.ID_ATTRIBUTE);
                if (
                    idAttr == null &&
                    (
                        registration.getDataProvider() != null ||
                        registration.getDescriptor().hasClass(ComponentClasses.FIELD) ||
                        registration.getDescriptor().hasClass(ComponentClasses.NEEDS_ID)
                    )
                )
                {
                    final String id = COUNTER.nextId();
                    setIdAttribute(elem, id);
                }

                ComponentInstanceRegistration instanceRegistration = new ComponentInstanceRegistration(
                    registration,
                    elem
                );
                elem.setComponentRegistration(instanceRegistration);
            }

            elem.setParent(parent);
        }

        for (ComponentModel componentModel : elem.children())
        {
            updateComponentRegsAndParentsRecursive(
                componentRegistry,
                componentModel,
                componentNames,
                elem
            );
        }
    }


    private static void setIdAttribute(ComponentModel elem, String id)
    {
        final Attributes attrs = elem.getAttrs();
        if (attrs == null)
        {
            final Attributes newAttrs = new Attributes();
            newAttrs.setAttribute(ComponentModel.ID_ATTRIBUTE, id);
            elem.setAttrs(newAttrs);
        }
        else
        {
            attrs.setAttribute(ComponentModel.ID_ATTRIBUTE, id);
        }
    }


    private final static IdCounter COUNTER = new IdCounter("id-");

    public static int findFlatIndex(View view, String componentId)
    {
        FindFlatResult res = new FindFlatResult();
        for (ComponentModel componentModel : view.getContent().values())
        {
            // XXX: handle 
            findFlatIndexRecursive(componentModel, componentId, res);
        }
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


    public static List<ComponentModel> findComponents(View view, Predicate<ComponentModel> predicate)
    {
        final Map<String, ComponentModel> content = view.getContent();
        if (content == null)
        {
            return Collections.emptyList();
        }

        List<ComponentModel> componentsFound = new ArrayList<>();
        for (ComponentModel componentModel : content.values())
        {
            findComponents(componentModel, predicate, componentsFound);
        }


        return componentsFound;
    }


    public static void findComponents(
        ComponentModel componentModel,
        Predicate<ComponentModel> predicate,
        List<ComponentModel> componentsFound
    )
    {
        if (predicate.test(componentModel))
        {
            componentsFound.add(componentModel);
        }

        for (ComponentModel model : componentModel.children())
        {
            findComponents(model, predicate, componentsFound);
        }
    }


    public static ComponentModel findParent(ComponentModel componentModel, Predicate<ComponentModel> consumer)
    {
        ComponentModel current = componentModel.getParent();

        while (current != null)
        {
            if (consumer.test(current))
            {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }


    private static class FindFlatResult
    {
        private int index = 0;
    }

    public final static class HasClassPredicate
        implements Predicate<ComponentModel>
    {
        private final String cls;


        public HasClassPredicate(String cls)
        {
            this.cls = cls;
        }

        @Override
        public boolean test(ComponentModel componentModel)
        {
            final ComponentInstanceRegistration registration = componentModel.getComponentRegistration();
            return registration != null && registration.getDescriptor().hasClass(cls);
        }
    }
}
