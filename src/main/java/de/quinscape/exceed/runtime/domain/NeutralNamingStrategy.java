package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainType;

/**
 * A neutral naming strategy that uses domain type names and property names as-is.
 */
public class NeutralNamingStrategy
    implements NamingStrategy
{

    @Override
    public String getTableName(String typeName)
    {
        return typeName;
    }


    @Override
    public String[] getFieldName(String tableName, String propertyName)
    {
        return new String[] { tableName, propertyName };
    }


    @Override
    public String getForeignKeyName(String typeName, String propertyName, String targetType, String targetProperty)
    {
        return "fk_" + typeName + "_" + propertyName;
    }


    @Override
    public String getPrimaryKeyName(String typeName)
    {
        return "pk_" + typeName;
    }

}
