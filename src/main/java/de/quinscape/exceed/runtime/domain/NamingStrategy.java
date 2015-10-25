package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Table;

public interface NamingStrategy
{
    String getTableName(String schema, DomainType type);

    String getFieldName(QueryDomainType queryDomainType, DomainProperty property);
}
