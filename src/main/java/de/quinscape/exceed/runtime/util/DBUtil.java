package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
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

        String schema = AppAuthentication.isAuthType(type) ? domainService.getAuthSchema() : domainService.getSchema();


        final ExceedDataSource dataSource = domainService.getDataSource(
            type.getDataSourceName()
        );
        final NamingStrategy namingStrategy = dataSource.getStorageConfiguration().getNamingStrategy();

        Table<Record> table = DSL.table(DSL.name(schema, namingStrategy.getTableName(type.getName())));

        if (alias == null)
        {
            return table;
        }
        else
        {
            return table.as(namingStrategy.getTableName(alias));
        }
    }


    public static Field<Object> jooqField(DomainType type, String propertyName)
    {
        final String domainTypeName = type.getName();
        final ExceedDataSource dataSource = type.getDomainService().getDataSource(
            type.getDataSourceName()
        );
        final NamingStrategy namingStrategy = dataSource.getStorageConfiguration().getNamingStrategy();
        return field(namingStrategy, domainTypeName, propertyName);
    }


    private static Field<Object> field(
        NamingStrategy namingStrategy, String domainTypeName, String propertyName
    )
    {
        return DSL.field(DSL.name(namingStrategy.getFieldName(domainTypeName, propertyName)));
    }


    public static Field<Object> jooqField(DomainService domainService, String domainType, String propertyName)
    {
        final ExceedDataSource dataSource = domainService.getDataSource(
            domainService.getDomainType(domainType).getDataSourceName()
        );
        final NamingStrategy namingStrategy = dataSource.getStorageConfiguration().getNamingStrategy();
        return field(namingStrategy, domainType, propertyName);
    }

}
