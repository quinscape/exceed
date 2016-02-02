package de.quinscape.exceed.model.view;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSON;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;
import org.svenson.StringBuilderSink;

import java.util.Collections;
import java.util.List;

public class ComponentModel
{

    public static final String STRING_MODEL_NAME = "[String]";

    private String name;

    private Attributes attrs;

    private List<ComponentModel> kids;

    private ComponentRegistration componentRegistration;


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
    public void setKids(List kids) throws ParseException
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
        AttributeValue value = attrs.getAttribute("id");
        return value != null ? (String) value.getValue() : null;
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
     * Returns <code>true</code> if the element represented by this model is full component or just a HTML tag.
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
        try
        {
            ComponentModel model = new ComponentModel();
            model.setName(STRING_MODEL_NAME);
            Attributes attrs = new Attributes(null);
            attrs.setAttribute("value", value);
            model.setAttrs(attrs);

            return model;
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException("Error creating string model", e);
        }
    }

    public static boolean isTextNode(ComponentModel componentModel)
    {
        return componentModel instanceof TextNode;
    }
}

