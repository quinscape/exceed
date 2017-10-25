package de.quinscape.exceed.runtime.domain.migration;

import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.migration.RenamePropertyModel;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.schema.DDLOperations;
import de.quinscape.exceed.runtime.schema.DatabaseColumn;

import java.util.List;
import java.util.Map;

public class RenamePropertyStep
    implements MigrationStep<RenamePropertyModel>
{
    private final DDLOperations ddlOperations;
    private final NamingStrategy namingStrategy;

    public RenamePropertyStep(DDLOperations ddlOperations, NamingStrategy namingStrategy)
    {
        this.ddlOperations = ddlOperations;
        this.namingStrategy = namingStrategy;
    }


    @Override
    public String describe(RenamePropertyModel renamePropertyModel)
    {
        return "Rename property '" + renamePropertyModel.getFrom() + "' in domain typpe '" + renamePropertyModel.getType() + " to '" + renamePropertyModel.getTo();
    }


    @Override
    public String check( DomainVersion domainVersion, RenamePropertyModel renamePropertyModel)
    {
        final String schema = domainVersion.getSchema();

        final List<String> tables = ddlOperations.listTables(schema);

        final String tableName = namingStrategy.getTableName(renamePropertyModel.getType());

        if (!tables.contains(tableName))
        {
            return "Cannot rename property in non-existing Table '" + tableName + "'";
        }

        final String[] name = namingStrategy.getFieldName(renamePropertyModel.getType(), renamePropertyModel.getFrom());
        final String fieldName = name[0] + "." + name[1];


        final Map<String, DatabaseColumn> columns = ddlOperations.listColumns(schema, tableName);

        if (!columns.containsKey(fieldName))
        {
            return "Field '" + fieldName + "' does not exist";
        }

        final String[] name2 = namingStrategy.getFieldName(renamePropertyModel.getType(), renamePropertyModel.getFrom());
        final String targetName = name2[0] + "." + name2[1];

        if (columns.containsKey(targetName))
        {
            return "Field '" + targetName + "' already exists";
        }
        return STATUS_OK;
    }

    @Override
    public void apply(DomainVersion domainVersion, RenamePropertyModel renameTypeModel)
    {
        final String schema = domainVersion.getSchema();
        ddlOperations.renameField(schema, renameTypeModel.getType(), renameTypeModel.getFrom(), renameTypeModel.getTo());
    }

}
