package de.quinscape.exceed.model.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentModelBuilder
{
    private final ComponentModel component;

    private List<ComponentModelBuilder> builders;


    public ComponentModelBuilder(String name)
    {
        component = new ComponentModel();
        component.setName(name);
    }

    public ComponentModelBuilder withAttribute(String name, String value)
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
        if (this.builders == null)
        {
            this.builders = new ArrayList<>();
        }

        Collections.addAll(this.builders, builders);
        return this;
    }

    public ComponentModel getComponent()
    {
        if (builders != null)
        {
            List<ComponentModel> kids = new ArrayList<>();
            for (ComponentModelBuilder builder : builders)
            {
                kids.add(builder.getComponent());
            }
            component.setKids(kids);
        }
        return component;
    }

    public static ComponentModelBuilder component(String name)
    {
        return new ComponentModelBuilder(name);
    }
}
