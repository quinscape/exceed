package de.quinscape.exceed.model.view;

import org.svenson.JSONParameters;
import org.svenson.JSONProperty;
import org.svenson.JSONable;
import org.svenson.util.JSONBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Contains all attributes of a component.
 *
 */
public class Attributes
    implements JSONable
{

    private Map<String,AttributeValue> attrs;

    public Attributes()
    {
        this(new HashMap<>());
    }

    public Attributes(
        @JSONParameters
        Map<String, String> attrs
    )
    {
        if (attrs != null)
        {
            this.attrs = convert(attrs);
        }
    }

    public void setAttribute(String name, AttributeValue value)
    {
        if (attrs == null)
        {
            attrs = new HashMap<>();
        }

        attrs.put(name,value);
    }

    private Map<String, AttributeValue> convert(Map<String, String> attrs)
    {
        Map<String, AttributeValue> newAttrs = new HashMap<>(attrs.size());
        for (Map.Entry<String, String> entry : attrs.entrySet())
        {
            newAttrs.put(entry.getKey(), AttributeValue.forValue(entry.getValue(), false));
        }
        return newAttrs;
    }


    public void setAttribute(String name, String value)
    {
        setAttribute(name, AttributeValue.forValue(value, false));
    }

    public void setAttribute(String name, Object value)
    {
        if (value instanceof String)
        {
            setAttribute(name, (String)value);
        }
        else
        {
            setAttribute(name, AttributeValue.toExpression(value));
        }
    }


    public AttributeValue getAttribute(String id)
    {
        if (attrs == null)
        {
            return null;
        }

        return attrs.get(id);
    }

    @JSONProperty(ignore = true)
    public Set<String> getNames()
    {
        return attrs.keySet();
    }


    @Override
    public String toJSON()
    {
        JSONBuilder b = JSONBuilder.buildObject();

        for (Map.Entry<String, AttributeValue> entry : attrs.entrySet())
        {
            b.property(entry.getKey(), entry.getValue());
        }

        return b.output();
    }
}
