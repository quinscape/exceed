package de.quinscape.exceed.model.component;

import de.quinscape.exceed.model.expression.ExpressionValue;
import org.svenson.JSONParameter;

public class ComponentViewContext
{
    private final String propertyType;

    private final ExpressionValue defaultValue;


    public ComponentViewContext(
        @JSONParameter("type")
        String propertyType,
        @JSONParameter("defaultValue")
        String defaultValue)
    {
        if (propertyType == null)
        {
            throw new IllegalArgumentException("propertyType can't be null");
        }

        this.propertyType = propertyType;
        this.defaultValue = ExpressionValue.forValue(defaultValue, true);
    }


    public String getPropertyType()
    {
        return propertyType;
    }


    public String getDefaultValue()
    {
        return defaultValue.getValue();
    }


    public ExpressionValue getDefaultAttributeValue()
    {
        return defaultValue;
    }
}
