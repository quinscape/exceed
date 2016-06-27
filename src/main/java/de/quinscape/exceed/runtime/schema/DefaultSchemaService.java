package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.ForeignKeyDefinition;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.NamingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the schema service that contains the default update algorithm and delegates the actual
 * operations to a {@link DDLOperations} instance.
 */
public class DefaultSchemaService
    implements SchemaService
{
    private final NamingStrategy namingStrategy;

    private final DDLOperations op;


    public DefaultSchemaService(NamingStrategy namingStrategy, DDLOperations op)
    {
        this.namingStrategy = namingStrategy;
        this.op = op;
    }


    @Override
    public void synchronizeSchema(RuntimeContext runtimeContext, List<DomainType> domainTypes)
    {
        final ApplicationModel applicationModel = runtimeContext.getRuntimeApplication().getApplicationModel();
        final String schemaName = applicationModel.getSchema();

        List<String> schemata = op.listSchemata();
        if (!schemata.contains(schemaName))
        {
            op.createSchema(schemaName);
        }

        List<String> tables = op.listTables(schemaName);

        for (DomainType type : domainTypes)
        {
            String tableName = namingStrategy.getTableName(type.getName());
            if (tables.contains(tableName))
            {
                op.dropKeys(runtimeContext, type);
            }
        }

        for (DomainType type : domainTypes)
        {

            String tableName = namingStrategy.getTableName(type.getName());
            if (tables.contains(tableName))
            {
                op.updateTable(runtimeContext, type);
            }
            else
            {
                op.createTable(runtimeContext, type);
            }
        }

        for (DomainType type : domainTypes)
        {
            op.createPrimaryKey(runtimeContext, type);
        }

        for (DomainType type : domainTypes)
        {
            for (DomainProperty domainProperty : type.getProperties())
            {
                final ForeignKeyDefinition foreignKeyDefinition = domainProperty.getForeignKeyDefinition();
                if (foreignKeyDefinition != null)
                {
                    op.createForeignKeys(runtimeContext, type, domainProperty);
                }
            }
        }
    }

    @Override
    public void removeSchema(RuntimeContext runtimeContext)
    {
        op.dropSchema(runtimeContext.getRuntimeApplication().getApplicationModel().getSchema());
    }
}

