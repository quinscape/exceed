package de.quinscape.exceed.model.context;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.view.AttributeValue;
import org.svenson.JSONProperty;

public class ScopedObjectModel
    extends ScopedElementModel
{
    private String type;

    private AttributeValue defaultValue;


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


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + getName() + '\''
            + "type = '" + type + '\''
            + ", defaultValue = " + defaultValue
            ;
    }
}

