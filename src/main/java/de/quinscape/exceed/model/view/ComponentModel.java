package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.annotation.DocumentedMapKey;
import de.quinscape.exceed.model.annotation.DocumentedModelType;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.component.PropType;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.ExpressionValueType;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;
import org.svenson.StringBuilderSink;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A view component within an exceed application. Following the react conventions, it is a full-fledged Javascript react
 * component when its name is uppercase and a simple HTML tag if its name is lowercase.
 */
public class ComponentModel
{
    public static final String STRING_MODEL_NAME = "[String]";

    public static final String ID_ATTRIBUTE = "id";

    /**
     * Attribute with which the provided context can be named.
     */
    public static final String VAR_ATTRIBUTE = "var";

    private String name;

    private Attributes attrs;

    private List<ComponentModel> kids;

    private ComponentInstanceRegistration componentRegistration;

    private ComponentModel parent;

    public ComponentModel()
    {

    }

    /**
     * Component name. Following react conventions, all components with uppercase names are exceed components and
     * all components with lowercase names are normal HTML tags.
     */
    @JSONProperty(priority = 10)
    public String getName()
    {
        return name;
    }


    /**
     * Map of attribute values <code>"value"</code> or expressions <code>"{ expr() }"</code>.
     */
    @JSONProperty(ignoreIfNull = true, priority = 5)
    @DocumentedModelType("Map attributeName -> Attribute")
    @DocumentedMapKey("attributeName")
    public Attributes getAttrs()
    {
        return attrs;
    }


    /**
     * List of children of this component model.
     */
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
        ExpressionValue value = attrs.getAttribute(ID_ATTRIBUTE);
        return value != null ? value.getValue() : null;
    }


    public ExpressionValue getAttribute(String key)
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

                ExpressionValue attribute = attrs.getAttribute(name);
                ExpressionValueType type = attribute.getType();
                Object value = attribute.getValue();
                if (type == ExpressionValueType.STRING)
                {
                    JSONUtil.DEFAULT_GENERATOR.quote(sb, (String) value);
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
    public void setComponentRegistration(ComponentInstanceRegistration componentRegistration)
    {
        this.componentRegistration = componentRegistration;

        if (componentRegistration != null && attrs != null)
        {
            for (Map.Entry<String, PropDeclaration> entry : componentRegistration.getDescriptor
                ().getPropTypes().entrySet())
            {
                final String attrName = entry.getKey();
                final PropDeclaration propDeclaration = entry.getValue();

                if (propDeclaration.getType() == PropType.CURSOR_EXPRESSION)
                {
                    final ExpressionValue value = attrs.getAttribute(attrName);
                    if (value != null && value.getType() == ExpressionValueType.STRING)
                    {
                        // if we have a cursor expression that is just a string, update it to a context expression.
                        attrs.setAttribute(
                            attrName,
                            ExpressionValue.forValue(
                                "context." + value.getValue(),
                                true
                            )
                        );
                    }
                }
            }
        }
    }


    public ComponentInstanceRegistration getComponentRegistration()
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

