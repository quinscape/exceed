package de.quinscape.exceed.model.domain;

public class DomainPropertyBuilder
{
    private String name = null;

    private String type = null;

    private String defaultValue = null;

    private boolean required = false;

    private Object typeParam = null;

    private int maxLength = 0;

    private String domainType = null;


    public DomainPropertyBuilder withName(String name)
    {
        this.name = name;
        return this;
    }


    public DomainPropertyBuilder withType(String type)
    {
        this.type = type;
        return this;
    }


    public DomainPropertyBuilder withDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
        return this;
    }


    public DomainPropertyBuilder setRequired(boolean required)
    {
        this.required = required;
        return this;
    }


    public DomainPropertyBuilder withTypeParam(Object typeParam)
    {
        this.typeParam = typeParam;
        return this;
    }


    public DomainPropertyBuilder withMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
        return this;
    }


    public DomainPropertyBuilder withDomainType(String domainType)
    {
        this.domainType = domainType;
        return this;
    }


    public DomainProperty build()
    {
        return new DomainProperty(name, type, defaultValue, required, typeParam, maxLength, domainType);
    }
}
