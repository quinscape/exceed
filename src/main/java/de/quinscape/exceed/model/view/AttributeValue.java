package de.quinscape.exceed.model.view;


import org.svenson.JSON;
import org.svenson.JSONParameter;
import org.svenson.JSONable;

public class AttributeValue
    implements JSONable
{
    private final AttributeValueType type;
    private final Object value;

    public AttributeValue(
        AttributeValueType type,
        Object value)
    {
        this.type = type;
        this.value = value;
    }

    public AttributeValueType getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "type = " + type
            + ", value = '" + value + '\''
            ;
    }

    @Override
    public String toJSON()
    {
        return JSON.defaultJSON().forValue(value);
    }
}

