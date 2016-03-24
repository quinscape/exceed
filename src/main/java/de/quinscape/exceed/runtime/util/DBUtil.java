package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class DBUtil
{
    public static Table<Record> jooqTableFor(DomainType type, String alias)
    {
        DomainService domainService = type.getDomainService();
        String schema = domainService.getSchema();
        NamingStrategy namingStrategy = type.getDomainService().getNamingStrategy();
        Table<Record> table = DSL.table(DSL.name(schema, namingStrategy.getTableName(type)));

        if (alias == null)
        {
            return table;
        }
        else
        {
            return table.as(alias);
        }
    }

}
