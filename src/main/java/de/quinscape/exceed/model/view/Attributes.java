package de.quinscape.exceed.model.view;

import org.svenson.AbstractDynamicProperties;
import org.svenson.JSONParameters;
import org.svenson.JSONProperty;
import org.svenson.JSONable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Attributes
{

    private Map<String,AttributeValue> attrs;
    private boolean idGenerated;

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
                attrValue = new AttributeValue(AttributeValueType.STRING, value);
            }
        }
        else if (value instanceof Long || value instanceof Integer)
        {
            attrValue = new AttributeValue(AttributeValueType.NUMBER, value);
        }
        else if (value instanceof Boolean)
        {
            attrValue = new AttributeValue(AttributeValueType.BOOLEAN, value);
        }
        else if (value instanceof Map)
        {
            attrValue = new AttributeValue(AttributeValueType.COMPLEX, value);
        }
        else
        {
            attrValue = null;
        }
        return attrValue;
    }

    public void setAttribute(String name, String value)
    {
        setAttribute(name, convertValue(value));
    }

    public void setAttribute(String name, int value)
    {
        setAttribute(name, new AttributeValue(AttributeValueType.NUMBER, value));
    }

    public void setAttribute(String name, boolean value)
    {
        setAttribute(name, new AttributeValue(AttributeValueType.BOOLEAN, value));
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

    void setGeneratedId(String id)
    {
        setAttribute("id", id);
        this.idGenerated = true;
    }


    @JSONProperty(ignore = true)
    public boolean isIdGenerated()
    {
        return idGenerated;
    }
    @JSONProperty(ignore = true)
    public Set<String> getNames()
    {
        return attrs.keySet();
    }
}
