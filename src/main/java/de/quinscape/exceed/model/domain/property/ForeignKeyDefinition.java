package de.quinscape.exceed.model.domain.property;

import de.quinscape.exceed.model.domain.type.DomainType;

/**
 * Domain foreign key definition.
 *
 * Note that not all StorageConfiguration might support foreign keys.
 */
public class ForeignKeyDefinition
{
    private String type;

    private String property = DomainType.ID_PROPERTY;

    /**
     * Target domain type name
     */
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    /**
     * Target property name. Default is <code>id</code>,
     */
    public String getProperty()
    {
        return property;
    }


    public void setProperty(String property)
    {
        this.property = property;
    }
}
