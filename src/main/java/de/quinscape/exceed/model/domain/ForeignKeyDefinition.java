package de.quinscape.exceed.model.domain;

/**
 * Domain foreign key definition.
 *
 * Note that not all StorageConfiguration
 */
public class ForeignKeyDefinition
{
    private String type;

    private String property = DomainType.ID_PROPERTY;


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public String getProperty()
    {
        return property;
    }


    public void setProperty(String property)
    {
        this.property = property;
    }
}
