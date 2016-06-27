package de.quinscape.exceed.runtime.domain;

public interface NamingStrategy
{
    String getTableName(String typeName);

    String[] getFieldName(String typeName, String propertyName);

    String getForeignKeyName(String typeName, String propertyName, String targetType, String targetProperty);

    String getPrimaryKeyName(String typeName);
}
