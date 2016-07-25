package de.quinscape.exceed.model.context;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.view.AttributeValue;
import org.svenson.JSONProperty;

public class ScopedPropertyModel
    extends ScopedElementModel
{
    private String type;

    private Object typeParam;

    private AttributeValue defaultValue;

    private boolean required;

    private int maxLength = -1;


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public String getDefaultValue()
    {
        return defaultValue != null ? defaultValue.getValue() : null;
    }

    @JSONProperty(ignore = true)
    public ASTExpression getDefaultValueExpression()
    {
        return defaultValue != null ? defaultValue.getAstExpression() : null;
    }


    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = AttributeValue.forValue(defaultValue, true);
    }


    public Object getTypeParam()
    {
        return typeParam;
    }


    public void setTypeParam(Object typeParam)
    {
        this.typeParam = typeParam;
    }


    public int getMaxLength()
    {
        return maxLength;
    }


    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }


    public boolean isRequired()
    {
        return required;
    }


    public void setRequired(boolean required)
    {
        this.required = required;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + getName() + '\''
            + "type = '" + type + '\''
            + ", typeParam = " + typeParam
            + ", defaultValue = " + defaultValue
            + ", required = " + required
            + ", maxLength = " + maxLength
            ;
    }
}
