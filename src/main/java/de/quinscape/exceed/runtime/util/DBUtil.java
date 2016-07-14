package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class DBUtil
{
    public static Table<Record> jooqTableFor(DomainType type, String alias)
    {
        DomainService domainService = type.getDomainService();
        String schema = domainService.getSchema();
        NamingStrategy namingStrategy = type.getDomainService().getStorageConfiguration(type.getName())
            .getNamingStrategy();
        Table<Record> table = DSL.table(DSL.name(schema, namingStrategy.getTableName(type.getName())));

        if (alias == null)
        {
            return table;
        }
        else
        {
            return table.as(alias);
        }
    }


    public static Field<Object> jooqField(DomainType type, String propertyName)
    {
        DomainService domainService = type.getDomainService();
        NamingStrategy namingStrategy = type.getDomainService().getStorageConfiguration(type.getName())
            .getNamingStrategy();
        return DSL.field(DSL.name(namingStrategy.getFieldName(type.getName(), propertyName)));
    }

}
