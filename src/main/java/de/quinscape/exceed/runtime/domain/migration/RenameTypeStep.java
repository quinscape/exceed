package de.quinscape.exceed.runtime.domain.migration;

import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.migration.RenameTypeModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.schema.DDLOperations;

import java.util.List;

public class RenameTypeStep
    implements MigrationStep<RenameTypeModel>
{
    private final DDLOperations ddlOperations;
    private final NamingStrategy namingStrategy;

    public RenameTypeStep(DDLOperations ddlOperations, NamingStrategy namingStrategy)
    {
        this.ddlOperations = ddlOperations;
        this.namingStrategy = namingStrategy;
    }


    @Override
    public String describe(RenameTypeModel renameTypeModel)
    {
        return "Rename domain type '" + renameTypeModel.getFrom() + " to '" + renameTypeModel.getTo();
    }


    @Override
    public String check(DomainVersion domainVersion, RenameTypeModel renameTypeModel)
    {
        final String schema = domainVersion.getSchema();

        final List<String> tables = ddlOperations.listTables(schema);

        final String tableName = namingStrategy.getTableName(renameTypeModel.getType());
        if (!tables.contains(tableName))
        {
            return "Table '" + tableName + "' does not exist.";
        }


        final String to = renameTypeModel.getTo();
        if (tables.contains(to))
        {
            return "Cannot rename Table '" + tableName + "' to '" + to + "' : Table already exists";
        }
        return STATUS_OK;


    }

    @Override
    public void apply(DomainVersion domainVersion, RenameTypeModel renameTypeModel)
    {
        final String schema = domainVersion.getSchema();
        ddlOperations.renameTable(schema, renameTypeModel.getType(), renameTypeModel.getTo());
    }
}
