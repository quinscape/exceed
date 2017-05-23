package de.quinscape.exceed.model.expression;

import org.svenson.JSONParameters;
import org.svenson.JSONProperty;
import org.svenson.JSONable;
import org.svenson.util.JSONBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates a map of {@link ExpressionValue}s.
 */
public class Attributes
    implements JSONable
{

    private Map<String,ExpressionValue> attrs;

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

    public void setAttribute(String name, ExpressionValue value)
    {
        if (attrs == null)
        {
            attrs = new HashMap<>();
        }

        attrs.put(name,value);
    }

    private Map<String, ExpressionValue> convert(Map<String, String> attrs)
    {
        Map<String, ExpressionValue> newAttrs = new HashMap<>(attrs.size());
        for (Map.Entry<String, String> entry : attrs.entrySet())
        {
            newAttrs.put(entry.getKey(), ExpressionValue.forValue(entry.getValue(), false));
        }
        return newAttrs;
    }


    public void setAttribute(String name, String value)
    {
        setAttribute(name, ExpressionValue.forValue(value, false));
    }

    public void setAttribute(String name, Object value)
    {
        if (value instanceof String)
        {
            setAttribute(name, (String)value);
        }
        else
        {
            setAttribute(name, ExpressionValue.toExpression(value));
        }
    }


    public ExpressionValue getAttribute(String id)
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

        for (Map.Entry<String, ExpressionValue> entry : attrs.entrySet())
        {
            b.property(entry.getKey(), entry.getValue());
        }

        return b.output();
    }


    public Attributes copy()
    {
        final Attributes copy = new Attributes(null);
        if (attrs == null || attrs.size() == 0)
        {
            return copy;
        }
        Map<String, ExpressionValue> values = new HashMap<>(attrs.size());

        for (Map.Entry<String, ExpressionValue> entry : attrs.entrySet())
        {
            values.put(entry.getKey(), ExpressionValue.forValue(entry.getValue().getValue(), false));
        }
        copy.attrs = values;
        return copy;
    }
}
