package de.quinscape.exceed.model.view;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementNode
{
    private String componentId;
    private Map<String,AttributeValue> attrs;
    private List<ElementNode> kids;

    public ElementNode(String componentId, Map<String, Object> attrs, ElementNode... kids)
    {
        this.componentId = componentId;
        this.attrs = attrs != null ? convert(attrs) : null;
        this.kids = kids == null || kids.length == 0 ? null : Arrays.asList(kids);
    }

    @JSONProperty(value = "id", priority = 10)
    public String getComponentId()
    {
        return componentId;
    }

    public void setComponentId(String componentId)
    {
        this.componentId = componentId;
    }

    @JSONProperty(ignoreIfNull = true, priority = 5)
    public Map<String, AttributeValue> getAttrs()
    {
        return attrs;
    }

    public void setAttribute(String name, String value)
    {

        setAttribute(name, new AttributeValue(AttributeValueType.STRING, value));
    }

    public void setAttribute(String name, AttributeValue value)
    {
        if (attrs == null)
        {
            attrs = new HashMap<>();
        }

        attrs.put(name, convertValue(value));
    }

    public void setAttrs(Map<String, Object> attrs)
    {
        Map<String, AttributeValue> newAttrs = convert(attrs);

        this.attrs = newAttrs;
    }

    private Map<String, AttributeValue> convert(Map<String, Object> attrs)
    {
        Map<String,AttributeValue> newAttrs = new HashMap<>(attrs.size());
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
            String stringValue = (String)value;
            if (((String) value).startsWith("{") && ((String) value).endsWith("}"))
            {
                attrValue = new AttributeValue(AttributeValueType.EXPRESSION, value);
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
            attrValue =  new AttributeValue(AttributeValueType.BOOLEAN, value);
        }
        else if (value instanceof Map)
        {
            attrValue =  new AttributeValue(AttributeValueType.COMPLEX, value);
        }
        else
        {
            attrValue = null;
        }
        return attrValue;
    }

    @JSONProperty(ignoreIfNull = true)
    public List<ElementNode> getKids()
    {
        return kids;
    }

    public void setKids(List<ElementNode> kids)
    {
        this.kids = kids;
    }

    public AttributeValue getAttribute(String key)
    {
        if (attrs == null)
            return null;
        return attrs.get(key);
    }
}
