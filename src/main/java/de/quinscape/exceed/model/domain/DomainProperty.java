package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.Model;
import org.svenson.JSONProperty;

public class DomainProperty
    extends Model
{
    private String name;
    private String type;
    private Object typeParam;
    private String defaultValue;
    private boolean required;
    private boolean translationNeeded;
    private int maxLength;

    public DomainProperty()
    {
        this(null, null, null, false, false);
    }

    public DomainProperty(String name, String type, String defaultValue, boolean required, boolean translationNeeded)
    {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
        this.translationNeeded = translationNeeded;
    }

    @JSONProperty(priority = 100)
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @JSONProperty(ignoreIfNull = true, priority = 60)
    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    @JSONProperty(priority = 80)
    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public boolean isTranslationNeeded()
    {
        return translationNeeded;
    }

    public void setTranslationNeeded(boolean translationNeeded)
    {
        this.translationNeeded = translationNeeded;
    }

    @JSONProperty(ignoreIfNull = true, priority = 90)
    public Object getTypeParam()
    {
        return typeParam;
    }

    public void setTypeParam(Object typeParam)
    {
        this.typeParam = typeParam;
    }

    @JSONProperty(ignoreIfNull = true, priority = 70)
    public int getMaxLength()
    {
        return maxLength;
    }

    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    @JSONProperty(ignore = true)
    public String getName()
    {
        return name;
    }
}
