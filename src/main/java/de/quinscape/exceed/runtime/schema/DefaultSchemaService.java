package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.ForeignKeyDefinition;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.util.AppAuthentication;

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
        final String schema = applicationModel.getConfigModel().getSchema();
        final String authSchema = applicationModel.getConfigModel().getAuthSchema();

        List<String> schemata = op.listSchemata();
        if (!schemata.contains(schema))
        {
            op.createSchema(schema);
        }
        if (!schemata.contains(authSchema))
        {
            op.createSchema(authSchema);
        }

        List<String> tables = op.listTables(schema);
        List<String> authTables = op.listTables(authSchema);

        for (DomainType type : domainTypes)
        {
            String tableName = namingStrategy.getTableName(type.getName());

            final boolean isAuthType = AppAuthentication.isAuthType(type);
            if (isAuthType ? authTables.contains(tableName) : tables.contains(tableName))
            {
                op.dropKeys(runtimeContext, type);
            }
        }

        for (DomainType type : domainTypes)
        {

            String tableName = namingStrategy.getTableName(type.getName());
            final boolean isAuthType = AppAuthentication.isAuthType(type);
            if (isAuthType ? authTables.contains(tableName) : tables.contains(tableName))
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
                final ForeignKeyDefinition foreignKeyDefinition = domainProperty.getForeignKey();
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
        op.dropSchema(runtimeContext.getRuntimeApplication().getApplicationModel().getConfigModel().getSchema());
    }
}

