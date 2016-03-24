package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.Model;
import org.svenson.JSONProperty;

public class DomainProperty
{
    private String name;

    private String type;

    private Object typeParam;

    private String defaultValue;

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
        this.defaultValue = defaultValue;
        this.required = required;
        this.maxLength = maxLength;
    }


    @JSONProperty(priority = 100)
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    @JSONProperty(priority = 90)
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    @JSONProperty(ignoreIfNull = true, priority = 80)
    public Object getTypeParam()
    {
        return typeParam;
    }


    public void setTypeParam(Object typeParam)
    {
        this.typeParam = typeParam;
    }


    @JSONProperty(priority = 70)
    public boolean isRequired()
    {
        return required;
    }


    public void setRequired(boolean required)
    {
        this.required = required;
    }


    @JSONProperty(ignoreIfNull = true, priority = 60)
    public int getMaxLength()
    {
        return maxLength;
    }


    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }

    @JSONProperty(ignoreIfNull = true, priority = 50)
    public String getDefaultValue()
    {
        return defaultValue;
    }


    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

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
            + ", defaultValue = '" + defaultValue + '\''
            + ", required = " + required
            + ", maxLength = " + maxLength
            + ", data = " + data
            ;
    }
}
