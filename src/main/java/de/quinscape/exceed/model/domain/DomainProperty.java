package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.view.AttributeValue;
import org.svenson.JSONProperty;

import java.util.Map;

public class DomainProperty
{
    private String name;

    private String type;

    private Object typeParam;

    private AttributeValue defaultValue;

    private boolean required;

    private int maxLength;

    private Object data;

    public DomainProperty()
    {
        this(null, null, null, false);
    }


    public DomainProperty(String name, String type, String defaultValue, boolean required)
    {
        this(name, type, defaultValue, required, null, 0);
    }


    public DomainProperty(String name, String type, String defaultValue, boolean required, Object typeParam, int
        maxLength)
    {
        this.name = name;
        this.type = type;
        this.typeParam = typeParam;
        setDefaultValue(defaultValue);
        this.required = required;
        this.maxLength = maxLength;
    }


    /**
     * Name of the property.
     *
     * @return
     */
    @JSONProperty(priority = 100)
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * Logical property type for this property.
     *
     * @return
     */
    @JSONProperty(priority = 90)
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    /**
     * Type parameter for the property. Set for List, Map, Enum.
     *
     * @return
     */
    @JSONProperty(ignoreIfNull = true, priority = 80)
    public Object getTypeParam()
    {
        return typeParam;
    }


    public void setTypeParam(Object typeParam)
    {
        this.typeParam = typeParam;
    }


    /**
     * Set true if this property is required.
     *
     * @return
     */
    @JSONProperty(priority = 70)
    public boolean isRequired()
    {
        return required;
    }


    public void setRequired(boolean required)
    {
        this.required = required;
    }


    /**
     * Maximum length for this property if applicable.
     *
     * @return
     */
    @JSONProperty(ignoreIfNull = true, priority = 60)
    public int getMaxLength()
    {
        return maxLength;
    }


    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }


    /**
     * Default value for this property.
     *
     * @return
     */
    @JSONProperty(ignoreIfNull = true, priority = 50)
    public String getDefaultValue()
    {
        return defaultValue != null ? defaultValue.getValue() : null;
    }


    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = AttributeValue.forValue(defaultValue, true);
    }


    @JSONProperty(ignore = true)
    public ASTExpression getDefaultValueExpression()
    {
        return defaultValue != null ? defaultValue.getAstExpression() : null;
    }


    /**
     * User data field. Not used by the system.
     *
     * @return
     */
    @JSONProperty(ignoreIfNull = true, priority = 40)
    public Object getData()
    {
        return data;
    }


    public void setData(Object data)
    {
        this.data = data;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", type = '" + type + '\''
            + ", typeParam = " + typeParam
            + ", defaultValue = " + defaultValue
            + ", required = " + required
            + ", maxLength = " + maxLength
            + ", domainType = '" + domainType + '\''
            + ", data = " + data
            ;
    }
}
