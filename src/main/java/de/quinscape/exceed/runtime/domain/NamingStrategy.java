package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;

public interface NamingStrategy
{
    String getTableName(DomainType type);

    String[] getFieldName(String tableName, String propertyName);
}
