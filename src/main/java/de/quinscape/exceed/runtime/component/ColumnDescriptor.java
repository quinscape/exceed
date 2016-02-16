package de.quinscape.exceed.runtime.component;

public class ColumnDescriptor
{
    private String domainType;

    private String name;


    public ColumnDescriptor()
    {
        this(null, null);
    }


    public ColumnDescriptor(String domainType, String name)
    {
        this.domainType = domainType;
        this.name = name;
    }


    public String getDomainType()
    {
        return domainType;
    }


    public void setDomainType(String domainType)
    {
        this.domainType = domainType;
    }


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }
}
