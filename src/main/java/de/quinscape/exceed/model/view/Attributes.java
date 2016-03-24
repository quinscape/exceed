package de.quinscape.exceed.model.view;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import org.svenson.JSONParameters;
import org.svenson.JSONProperty;
import org.svenson.JSONable;
import org.svenson.util.JSONBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        Map<String, Object> attrs
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

    private Map<String, AttributeValue> convert(Map<String, Object> attrs)
    {
        Map<String, AttributeValue> newAttrs = new HashMap<>(attrs.size());
        for (Map.Entry<String, Object> entry : attrs.entrySet())
        {
            newAttrs.put(entry.getKey(), convertValue(entry.getValue()));
        }
        return newAttrs;
    }

    private AttributeValue convertValue(Object value)
    {
        try
        {
            AttributeValue attrValue;
            if (value instanceof String)
            {
                String stringValue = (String) value;
                if (stringValue.startsWith("{") && stringValue.endsWith("}"))
                {
                    attrValue = new AttributeValue(AttributeValueType.EXPRESSION, formatExpression(stringValue));
                }
                else
                {
                    attrValue = new AttributeValue(AttributeValueType.STRING, (String) value);
                }
            }
            else if (value instanceof Long || value instanceof Integer || value instanceof Boolean)
            {
                attrValue = new AttributeValue(AttributeValueType.STRING, String.valueOf(value));
            }
            else if (value instanceof Collection || value instanceof Map)
            {
                throw new IllegalArgumentException("Invalid complex attribute value: " + value);
            }
            else
            {
                attrValue = null;
            }
            return attrValue;
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    public void setAttribute(String name, String value)
    {
        setAttribute(name, convertValue(value));
    }

    public void setAttribute(String name, Object value)
    {
        setAttribute(name, convertValue(value));
    }

    static String formatExpression(String expr)
    {
        if (expr.charAt(1) != ' ')
        {
            expr = "{ " + expr.substring(1);
        }

        int closingBrace = expr.length() - 1;
        int beforeBrace = expr.length() - 2;
        if (expr.charAt(beforeBrace) != ' ')
        {
            expr = expr.substring(0,closingBrace) + " }";
        }

        return expr;
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
