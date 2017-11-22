package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.ForeignKeyDefinition;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * Default implementation of the schema service that contains the default update algorithm and delegates the actual
 * operations to a {@link DDLOperations} instance.
 */
public class DefaultSchemaService
    implements SchemaService
{
    private final static Logger log = LoggerFactory.getLogger(DefaultSchemaService.class);


    private final NamingStrategy namingStrategy;

    private final DDLOperationsFactory opFactory;


    public DefaultSchemaService(NamingStrategy namingStrategy, DDLOperationsFactory opFactory)
    {
        this.namingStrategy = namingStrategy;
        this.opFactory = opFactory;
    }


    @Override
    public void synchronizeSchema(RuntimeContext runtimeContext, ExceedDataSource dataSource, List<DomainType> domainTypes)
    {

        log.info("Synchronize schema: {}, {}", dataSource, domainTypes);

        final ApplicationModel applicationModel = runtimeContext.getRuntimeApplication().getApplicationModel();
        final String schema = applicationModel.getConfigModel().getSchema();
        final String authSchema = applicationModel.getConfigModel().getAuthSchema();

        final DDLOperations ops = opFactory.create(runtimeContext, dataSource);
        try
        {
            List<String> schemata = ops.listSchemata(runtimeContext);
            if (!schemata.contains(schema))
            {
                ops.createSchema(runtimeContext, schema);
            }
            if (!schema.equals(authSchema) && !schemata.contains(authSchema))
            {
                ops.createSchema(runtimeContext, authSchema);
            }

            List<String> tables = ops.listTables(runtimeContext, schema);
            List<String> authTables = ops.listTables(runtimeContext, authSchema);

            for (DomainType type : domainTypes)
            {
                String tableName = namingStrategy.getTableName(type.getName());

                final boolean isAuthType = AppAuthentication.isAuthType(type);
                if (isAuthType ? authTables.contains(tableName) : tables.contains(tableName))
                {
                    ops.dropKeys(runtimeContext, type);
                }
            }

            for (DomainType type : domainTypes)
            {

                String tableName = namingStrategy.getTableName(type.getName());
                final boolean isAuthType = AppAuthentication.isAuthType(type);
                if (isAuthType ? authTables.contains(tableName) : tables.contains(tableName))
                {
                    ops.updateTable(runtimeContext, type);
                }
                else
                {
                    ops.createTable(runtimeContext, type);
                }
            }

            for (DomainType type : domainTypes)
            {
                ops.createPrimaryKey(runtimeContext, type);
            }

            for (DomainType type : domainTypes)
            {
                for (DomainProperty domainProperty : type.getProperties())
                {
                    final ForeignKeyDefinition foreignKeyDefinition = domainProperty.getForeignKey();
                    if (foreignKeyDefinition != null)
                    {
                        ops.createForeignKeys(runtimeContext, type, domainProperty);
                    }
                }
            }
        }
        finally
        {
            ops.destroy();
        }

    }

    @Override
    public void removeSchema(RuntimeContext runtimeContext, ExceedDataSource dataSource)
    {
        final DDLOperations op = opFactory.create(runtimeContext, dataSource);

        final ApplicationConfig configModel = runtimeContext.getApplicationModel().getConfigModel();
        op.dropSchema(runtimeContext, configModel.getSchema());
    }
}

