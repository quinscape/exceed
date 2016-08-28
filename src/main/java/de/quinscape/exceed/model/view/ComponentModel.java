package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSON;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;
import org.svenson.StringBuilderSink;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ComponentModel
{

    public static final String STRING_MODEL_NAME = "[String]";

    private String name;

    private Attributes attrs;

    private List<ComponentModel> kids;

    private ComponentRegistration componentRegistration;

    private ComponentModel parent;


    public ComponentModel()
    {

    }

    @JSONProperty(priority = 10)
    public String getName()
    {
        return name;
    }


    @JSONProperty(ignoreIfNull = true, priority = 5)
    public Attributes getAttrs()
    {
        return attrs;
    }


    public List<ComponentModel> getKids()
    {
        return kids;
    }


    @JSONProperty(ignoreIfNull = true)
    @JSONTypeHint(ComponentModel.class)
    public void setKids(List kids)
    {
        for (int i = 0; i < kids.size(); i++)
        {
            Object o = kids.get(i);
            if (o instanceof String)
            {
                kids.set(i, ComponentModel.forString((String)o));
            }
            else if (!(o instanceof ComponentModel))
            {
                throw new IllegalStateException("Cannot add " + o + " to " + this);
            }
        }

        this.kids = (List<ComponentModel>) kids;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * Null-safe list of children
     *
     * @return list of children, might be empty.
     */
    public List<ComponentModel> children()
    {
        if (kids == null)
        {
            return Collections.emptyList();
        }
        return kids;
    }


    @JSONProperty(ignore = true)
    public String getComponentId()
    {
        if (attrs == null)
        {
            return null;
        }
        AttributeValue value = attrs.getAttribute(DomainType.ID_PROPERTY);
        return value != null ? value.getValue() : null;
    }


    public AttributeValue getAttribute(String key)
    {
        if (attrs == null)
        {
            return null;
        }

        return attrs.getAttribute(key);
    }


    public void setAttrs(Attributes attrs)
    {
        final AttributeValue idAttr = attrs.getAttribute(DomainType.ID_PROPERTY);
        if (idAttr != null)
        {
            if (idAttr.getValue().equals(RuntimeApplication.RUNTIME_INFO_NAME))
            {
                throw new IllegalStateException(RuntimeApplication.RUNTIME_INFO_NAME + " is a reserved component id.");
            }
        }

        this.attrs = attrs;
    }


    @Override
    public String toString()
    {
        StringBuilderSink sb = new StringBuilderSink();
        sb.append("<");
        sb.append(name);

        if (attrs != null)
        {
            for (String name : attrs.getNames())
            {
                sb.append(" ");
                sb.append(name);
                sb.append("=");

                AttributeValue attribute = attrs.getAttribute(name);
                AttributeValueType type = attribute.getType();
                Object value = attribute.getValue();
                if (type == AttributeValueType.STRING)
                {
                    JSON.defaultJSON().quote(sb, (String) value);
                }
                else
                {
                    sb.append(value);
                }
            }
        }
        sb.append(">");
        return sb.getContent();
    }


    /**
     * Returns <code>true</code> if the element represented by this model is full component and <code>false</code> if it is just a HTML tag.
     * <p>
     * Exceed follows the ReactJs convention that component names start with an upper case letter and HTML tags
     * with a lower case letter.
     * </p>
     *
     * @return
     */
    @JSONProperty(ignore = true)
    public boolean isComponent()
    {
        return Character.isUpperCase(name.charAt(0));
    }


    @JSONProperty(ignore = true)
    public void setComponentRegistration(ComponentRegistration componentRegistration)
    {
        this.componentRegistration = componentRegistration;
    }


    public ComponentRegistration getComponentRegistration()
    {
        return componentRegistration;
    }


    @Override
    public boolean equals(Object obj)
    {
        return obj == this || obj instanceof ComponentModel && ((ComponentModel) obj).getComponentId().equals
            (getComponentId());

    }


    @Override
    public int hashCode()
    {
        return Util.hashcodeOver(getComponentId());
    }


    /**
     * Creates a model wrapper for a string or expression value that can be used as a component child in view models.
     *
     * @param value String or expression content
     *
     * @return component with {@link #STRING_MODEL_NAME} as name and the content as "value" attribute.
     */
    public static ComponentModel forString(String value)
    {
        ComponentModel model = new ComponentModel();
        model.setName(STRING_MODEL_NAME);
        Attributes attrs = new Attributes(null);
        attrs.setAttribute("value", value);
        model.setAttrs(attrs);

        return model;
    }

    public static boolean isTextNode(ComponentModel componentModel)
    {
        return componentModel instanceof TextNode;
    }


    public ComponentModel getParent()
    {
        return parent;
    }


    @JSONProperty(ignore = true)
    public void setPos(Object pos)
    {
        // ignore
    }


    @JSONProperty(ignore = true)
    public void setParent(ComponentModel parent)
    {
        this.parent = parent;
    }


    public ComponentModel find(Predicate<ComponentModel> predicate)
    {
        if (predicate.test(this))
        {
            return this;
        }

        if (kids != null)
        {
            for (ComponentModel kid : kids)
            {
                ComponentModel found = kid.find(predicate);
                if (found != null)
                {
                    return found;
                }
            }
        }
        return null;
    }


    public void walk(Consumer<ComponentModel> c)
    {
        c.accept(this);
        if (kids != null)
        {
            for (ComponentModel kid : kids)
            {
                kid.walk(c);
            }
        }
    }

    public ComponentModel copy()
    {
        ComponentModel copy = new ComponentModel();
        copy.setName(name);

        if (attrs != null)
        {
            copy.setAttrs(attrs.copy());
        }

        if (kids != null && kids.size() > 0)
        {
            List<ComponentModel> list = new ArrayList<>(kids.size());
            for (ComponentModel kid : kids)
            {
                list.add(kid.copy());
            }

            copy.setKids(list);
        }
        return copy;
    }

    @PostConstruct
    public void init()
    {
        if (name == null)
        {
            throw new IllegalStateException("Component model must have a name");
        }
    }
}

