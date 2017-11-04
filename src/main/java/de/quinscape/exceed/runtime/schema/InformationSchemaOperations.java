package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.ForeignKeyDefinition;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InformationSchemaOperations
    implements DDLOperations
{
    private final static int TEXT_LIMIT = 256;

    private static Logger log = LoggerFactory.getLogger(InformationSchemaOperations.class);

    private final DataSource dataSource;

    private final JdbcTemplate template;

    private final NamingStrategy namingStrategy;


    public InformationSchemaOperations(DataSource dataSource, NamingStrategy namingStrategy)
    {
        this.dataSource = dataSource;
        this.namingStrategy = namingStrategy;
        this.template = new JdbcTemplate(dataSource);
    }


    @Override
    public List<String> listSchemata()
    {
        return template.query("select schema_name\n" +
            "from information_schema.schemata", (rs, rowNum) ->
        {
            return rs.getString(1);
        });
    }


    @Override
    public void dropSchema(String name)
    {
        template.execute("DROP SCHEMA " + name + " CASCADE");
    }


    @Override
    public void createSchema(String name)
    {
        template.execute("CREATE SCHEMA " + name);
    }


    @Override
    public List<String> listTables(String schema)
    {
        return template.query("SELECT table_name from information_schema.tables where table_schema = ?", new
            Object[]{schema}, (rs, rowNum) ->
        {
            return rs.getString(1);
        });
    }


    @Override
    public void createTable(RuntimeContext runtimeContext, DomainType type)
    {
        final String schemaName = getSchemaName(runtimeContext, type);
        final String tableName = namingStrategy.getTableName(type.getName());

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(schemaName).append('.').append(tableName).append("\n(");

        for (Iterator<DomainProperty> iterator = type.getProperties().iterator(); iterator.hasNext(); )
        {
            DomainProperty domainProperty = iterator.next();
            sql.append("  ").append(columnClause(runtimeContext, type, domainProperty));
            if (iterator.hasNext())
            {
                sql.append(",\n");
            }
        }
        sql.append("\n)");

        log.info("SQL:\n{}", sql);
        template.execute(sql.toString());

    }


    private String columnClause(RuntimeContext runtimeContext, DomainType type, DomainProperty domainProperty)
    {
        final FieldType sqlType = getSQLType(runtimeContext, domainProperty);
        String typeExpr = sqlType.getSqlExpression(runtimeContext, domainProperty);
        final String[] fieldName = namingStrategy.getFieldName(type.getName(), domainProperty.getName());
        return fieldName[1] + " " + typeExpr + (domainProperty.isRequired() ? " NOT NULL" : "") + (domainProperty.isUnique() ? " UNIQUE" : "");
    }


    private FieldType getSQLType(RuntimeContext runtimeContext, DomainProperty domainProperty)
    {
        final PropertyConverter converter = domainProperty.getPropertyType();

        if (converter == null)
        {
            throw new IllegalStateException("No converter registered for domain property: " + domainProperty);
        }

        final Class javaType = converter.getJavaType();
        if (javaType.equals(String.class))
        {
            final int maxLength = domainProperty.getMaxLength();

            if (maxLength <= 0 || maxLength >= TEXT_LIMIT)
            {
                return FieldType.TEXT;
            }
            else
            {
                return FieldType.CHARACTER_VARYING;
            }
        }
        else if (javaType.equals(Integer.class))
        {
            return FieldType.INTEGER;
        }
        else if (javaType.equals(Boolean.class))
        {
            return FieldType.BOOLEAN;
        }
        else if (javaType.equals(Long.class))
        {
            return FieldType.BIGINT;
        }
        else if (javaType.equals(Timestamp.class))
        {
            return FieldType.TIMESTAMP_WITHOUT_TIME_ZONE;
        }
        else if (javaType.equals(Date.class))
        {
            return FieldType.DATE;
        }
        else if (javaType.equals(BigDecimal.class))
        {
            return FieldType.DECIMAL;
        }
        else
        {
            throw new IllegalStateException("Cannot get SQL type for unhandled java type: " + javaType);
        }
    }


    @Override
    public void updateTable(RuntimeContext runtimeContext, DomainType type)
    {
        final String schemaName = getSchemaName(runtimeContext, type);
        final String tableName = namingStrategy.getTableName(type.getName());

        Map<String, DatabaseColumn> columnsMap = listColumns(schemaName, tableName);
        List<DatabaseKey> keys = keysMap(schemaName, tableName);

        Set<String> updatedColumns = new HashSet<>();

        final String alterTableRoot = "ALTER TABLE " + schemaName + "." + tableName + " ";

        for (DomainProperty domainProperty : type.getProperties())
        {
            final FieldType fieldType = getSQLType(runtimeContext, domainProperty);
            final String name = namingStrategy.getFieldName(type.getName(), domainProperty.getName())[1];
            final boolean nullable = !domainProperty.isRequired();
            final boolean unique = domainProperty.isUnique();

            final String sqlType = fieldType.getSqlExpression(runtimeContext,domainProperty);

            updatedColumns.add(name);

            DatabaseColumn info = columnsMap.get(name);


            if (info == null)
            {
                // -> create column
                template.execute(alterTableRoot + "ADD COLUMN " + columnClause(runtimeContext, type, domainProperty));
            }
            else
            {
                // -> update column
                final boolean dbIsNullable = info.isNullable();
                if (nullable != dbIsNullable)
                {
                    template.execute(alterTableRoot + "ALTER COLUMN " + name + (nullable ? " DROP NOT NULL" : " SET " +
                        "NOT NULL"));
                }

                final DatabaseKey uniqueConstraint = findUniqueConstraint(keys, name);

                final boolean dbIsUnique = uniqueConstraint != null;
                if (unique && !dbIsUnique)
                {
                    final String uniqueConstraintName = namingStrategy.getUniqueConstraintName(type.getName(), domainProperty.getName());
                    template.execute(alterTableRoot + "ADD CONSTRAINT " + uniqueConstraintName + " UNIQUE (" + name + ");");
                }
                else if (!unique && dbIsUnique)
                {
                    template.execute(alterTableRoot + "DROP CONSTRAINT " + uniqueConstraint.name);
                }

//                boolean dbIsUnique = info.isNullable()
//                if (nullable != dbIsNullable)
//                {
//                    template.execute(alterTableRoot + "ALTER COLUMN " + name + (nullable ? " DROP NOT NULL" : " SET " +
//                        "NOT NULL"));
//                }
                Integer dbLength = info.getCharacterMaximumLength();

                String dbType;
                if (dbLength == null)
                {
                    dbType = info.getDataType();
                }
                else
                {
                    dbType = info.getDataType() + "(" + dbLength + ")";
                }

                log.debug("Compare type: {} and {}", sqlType, dbType);

                if (!sqlType.equals(dbType))
                {
                    template.execute(alterTableRoot + "ALTER COLUMN " + name + " TYPE " + sqlType);
                }
            }
        }

        HashSet<String> leftovers = new HashSet<>(columnsMap.keySet());
        leftovers.removeAll(updatedColumns);

        for (String name : leftovers)
        {
            template.execute(alterTableRoot + "DROP COLUMN " + name + " CASCADE");
        }
    }


    private DatabaseKey findUniqueConstraint(List<DatabaseKey> keys, String name)
    {
        for (DatabaseKey key : keys)
        {
            if (key.column.equals(name) && !key.fk)
            {
                return key;
            }
        }
        return null;
    }


    @Override
    public void dropKeys(RuntimeContext runtimeContext, DomainType type)
    {
        final String schemaName = getSchemaName(runtimeContext, type);
        final String tableName = namingStrategy.getTableName(type.getName());

        List<DatabaseKey> keys = keysMap(schemaName, tableName);

        for (DatabaseKey key : keys)
        {
            template.execute("ALTER TABLE " + schemaName + "." + tableName + " DROP CONSTRAINT " + key.name + " " +
                "CASCADE");
        }

    }


    @Override
    public void createPrimaryKey(RuntimeContext runtimeContext, DomainType type)
    {
        final String schemaName = getSchemaName(runtimeContext, type);
        final String tableName = namingStrategy.getTableName(type.getName());

        // add primary key
        template.execute("ALTER TABLE " + schemaName + "." + tableName + " ADD " + pkConstraint(type));

    }


    private String pkConstraint(DomainType type)
    {
        return "CONSTRAINT " + namingStrategy.getPrimaryKeyName(type.getName()) + " PRIMARY KEY (id)";
    }


    @Override
    public void createForeignKeys(RuntimeContext runtimeContext, DomainType type, DomainProperty domainProperty)
    {
        final String schemaName = getSchemaName(runtimeContext, type);
        final String tableName = namingStrategy.getTableName(type.getName());

        final String alterTableRoot = "ALTER TABLE " + schemaName + "." + tableName + " ";

        final DomainType targetType = runtimeContext.getDomainService().getDomainType(
            domainProperty.getForeignKey().getType());

        final String targetSchema = getSchemaName(runtimeContext, targetType);

        template.execute(alterTableRoot + "ADD " + fkConstraint(runtimeContext, targetSchema, type, domainProperty,
            domainProperty.getForeignKey()));

    }


    private String getSchemaName(RuntimeContext runtimeContext, DomainType type)
    {
        final ApplicationConfig configModel = runtimeContext.getApplicationModel().getConfigModel();
        return AppAuthentication.isAuthType(type) ? configModel.getAuthSchema() : configModel.getSchema();
    }


    @Override
    public void renameTable(String schema, String from, String to)
    {
        final String tableFrom = namingStrategy.getTableName(from);
        final String tableTo = namingStrategy.getTableName(to);

        template.execute("ALTER TABLE " + schema + "." + tableFrom + " RENAME TO " + tableTo);
    }


    private String fkConstraint(RuntimeContext runtimeContext, String schemaName, DomainType type, DomainProperty
        domainProperty, ForeignKeyDefinition
        foreignKeyDefinition)
    {
        DomainType targetType = runtimeContext.getDomainService().getDomainType(foreignKeyDefinition.getType());


        String keyName = namingStrategy.getForeignKeyName(type.getName(), domainProperty.getName(),
            foreignKeyDefinition.getType(), foreignKeyDefinition.getProperty());
        String targetName = namingStrategy.getTableName(targetType.getName());

        final String[] fieldName = namingStrategy.getFieldName(type.getName(), domainProperty.getName());
        final String[] targetField = namingStrategy.getFieldName(targetType.getName(), foreignKeyDefinition.getProperty());
        return "CONSTRAINT " + keyName +
            " FOREIGN KEY (" + fieldName[1] + ") REFERENCES " + schemaName + "." + targetName +
            " (" + targetField[1] + ") MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
    }


    public Map<String, DatabaseColumn> listColumns(String schemaName, String tableName)
    {

        ColumnMapper columnMapper = new ColumnMapper();
        template.query("SELECT * FROM information_schema.columns where table_schema = ? and table_name = ?", new
            Object[]{schemaName, tableName}, columnMapper);
        return columnMapper.getColumnMap();
    }


    @Override
    public void renameField(String schema, String type, String from, String to)
    {
        final String tableFrom = namingStrategy.getTableName(type);

        final String[] qualifiedSource = namingStrategy.getFieldName(type, from);
        final String[] qualifiedTarget = namingStrategy.getFieldName(type, to);

        template.execute(
            "ALTER TABLE " + schema + "." + tableFrom +
                " RENAME COLUMN " + qualifiedSource[1] + " TO " + qualifiedTarget[1]);

    }

    private List<DatabaseKey> keysMap(String schemaName, String tableName)
    {
        return template.query("SELECT DISTINCT constraint_name, column_name, " +
                "position_in_unique_constraint " +
                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE where constraint_schema = ? and table_name = ?;",
            new Object[]{
                schemaName,
                tableName
            },
            (rs, rowNum) -> new DatabaseKey(rs.getString(1), rs.getString(2), rs.getObject(3) != null)
        );
    }


    /**
     * Maps information_schema.columns rows
     */
    private class ColumnMapper
        implements RowCallbackHandler
    {
        private final Map<String, DatabaseColumn> columnMap;


        private ColumnMapper()
        {
            columnMap = new HashMap<>();
        }


        @Override
        public void processRow(ResultSet rs) throws SQLException
        {
            String columnName = rs.getString("column_name");
            String dataType = rs.getString("data_type");
            Integer characterMaximumLength = (Integer) rs.getObject("character_maximum_length");
            boolean nullable = rs.getString("is_nullable").equals("YES");

            columnMap.put(columnName, new DatabaseColumn(dataType, characterMaximumLength, nullable));
        }


        public Map<String, DatabaseColumn> getColumnMap()
        {
            return columnMap;
        }
    }
}
