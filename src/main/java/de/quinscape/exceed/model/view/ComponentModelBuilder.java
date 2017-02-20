package de.quinscape.exceed.model.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentModelBuilder
    implements ComponentSource
{
    private final ComponentModel component;

    private List<ComponentSource> children;

    public ComponentModelBuilder(String name)
    {
        component = new ComponentModel();
        component.setName(name);
    }


    public ComponentModelBuilder withAttribute(String name, Object value)
    {
        Attributes attrs = component.getAttrs();
        if (attrs == null)
        {
            attrs = new Attributes();
            component.setAttrs(attrs);
        }

        attrs.setAttribute(name, value);

        return this;
    }

    public ComponentModelBuilder withKids(ComponentModelBuilder... builders)
    {
        if (this.children == null)
        {
            this.children = new ArrayList<>();
        }

        Collections.addAll(this.children, builders);
        return this;
    }

    public ComponentModelBuilder withKids(final List<ComponentModel> components)
    {
        if (this.children == null)
        {
            this.children = new ArrayList<>();
        }

        this.children.add(() -> components);
        return this;
    }

    public ComponentModel getComponent()
    {
        if (children != null)
        {
            List<ComponentModel> kids = new ArrayList<>();
            for (ComponentSource src : children)
            {
                kids.addAll(src.getComponents());
            }

            component.setKids(kids);
        }
        return component;
    }

    public static ComponentModelBuilder component(String name)
    {
        return new ComponentModelBuilder(name);
    }


    @Override
    public List<ComponentModel> getComponents()
    {
        return Collections.singletonList(getComponent());
    }
}
