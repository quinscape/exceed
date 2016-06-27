package de.quinscape.exceed.component;

import de.quinscape.exceed.model.view.AttributeValue;
import org.svenson.JSONParameter;

public class ComponentViewContext
{
    private final String objectType;

    private final String propertyType;

    private final AttributeValue defaultValue;


    public ComponentViewContext(
        @JSONParameter("objectType")
        String objectType,
        @JSONParameter("propertyType")
        String propertyType,
        @JSONParameter("defaultValue")
        String defaultValue)
    {
        if (objectType != null && propertyType != null)
        {
            throw new IllegalStateException("objectType and propertyType cannot be both set.");
        }

        this.objectType = objectType;
        this.propertyType = propertyType;
        this.defaultValue = AttributeValue.forValue(defaultValue, true);
    }


    public String getObjectType()
    {
        return objectType;
    }


    public String getPropertyType()
    {
        return propertyType;
    }


    public String getDefaultValue()
    {
        return defaultValue.getValue();
    }


    public AttributeValue getDefaultAttributeValue()
    {
        return defaultValue;
    }


    public boolean isList()
    {
        return objectType == null && propertyType == null;
    }
}
