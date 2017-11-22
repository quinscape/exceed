package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.ForeignKeyDefinition;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.staging.SchemaUpdateMode;
import de.quinscape.exceed.runtime.ExceedProperties;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InformationSchemaOperations
    implements DDLOperations
{
    private final static Logger log = LoggerFactory.getLogger(InformationSchemaOperations.class);

    private final static int TEXT_LIMIT = 256;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final Environment env;

    private final String appName;

    private final NamingStrategy namingStrategy;

    private final JdbcTemplate template;

    private final StringBuilder statements;

    public InformationSchemaOperations(
        Environment env,
        String appName,
        NamingStrategy namingStrategy,
        ExceedDataSource dataSource
    )
    {
        this.env = env;
        this.appName = appName;
        this.namingStrategy = namingStrategy;
        final SchemaUpdateMode schemaUpdateMode = dataSource.getDataSourceModel().getSchemaUpdateMode();

        switch (schemaUpdateMode)
        {
            case NONE:
                // do nothing
                this.template = null;
                statements = null;
                break;

            case DUMP:
                // log statements
                this.template = null;
                statements = new StringBuilder();
                break;

            case UPDATE:
                // update schema
                this.template = new JdbcTemplate(dataSource.getDataSource());
                statements = null;
                break;

            default:
                throw new IllegalStateException("Unhandled schema update mode: " + schemaUpdateMode);
        }
    }

    @Override
    public List<String> listSchemata(RuntimeContext runtimeContext)
    {

        return queryInformationSchema("select schema_name\n" +
            "from information_schema.schemata", (rs, rowNum) ->
        {
            return rs.getString(1);
        });
    }


    @Override
    public void dropSchema(RuntimeContext runtimeContext, String name)
    {
        executeDDL("DROP SCHEMA " + name + " CASCADE");
    }


    private void executeDDL(String sql)
    {
        if (statements != null)
        {
            statements.append(sql).append(LINE_SEPARATOR);
        }

        if (template != null)
        {
            template.execute(sql);
        }
    }

    @Override
    public void createSchema(RuntimeContext runtimeContext, String name)
    {
        executeDDL("CREATE SCHEMA " + name);
    }


    @Override
    public List<String> listTables(RuntimeContext runtimeContext, String schema)
    {
        return queryInformationSchema("SELECT table_name from information_schema.tables where table_schema = ?", new
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
        executeDDL(sql.toString());

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
        final PropertyConverter converter = PropertyType.get(runtimeContext, domainProperty);

        if (converter == null)
        {
            throw new IllegalStateException("No converter registered for domain property: " + ExpressionUtil.describe(domainProperty));
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

        Map<String, DatabaseColumn> columnsMap = listColumns(runtimeContext, schemaName, tableName);
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
                executeDDL(alterTableRoot + "ADD COLUMN " + columnClause(runtimeContext, type, domainProperty));
            }
            else
            {
                // -> update column
                final boolean dbIsNullable = info.isNullable();
                if (nullable != dbIsNullable)
                {
                    executeDDL(alterTableRoot + "ALTER COLUMN " + name + (nullable ? " DROP NOT NULL" : " SET " +
                        "NOT NULL"));
                }

                final DatabaseKey uniqueConstraint = findUniqueConstraint(keys, name);

                final boolean dbIsUnique = uniqueConstraint != null;
                if (unique && !dbIsUnique)
                {
                    final String uniqueConstraintName = namingStrategy.getUniqueConstraintName(type.getName(), domainProperty.getName());
                    executeDDL(alterTableRoot + "ADD CONSTRAINT " + uniqueConstraintName + " UNIQUE (" + name + ");");
                }
                else if (!unique && dbIsUnique)
                {
                    executeDDL(alterTableRoot + "DROP CONSTRAINT " + uniqueConstraint.name);
                }

//                boolean dbIsUnique = info.isNullable()
//                if (nullable != dbIsNullable)
//                {
//                    executeDDL(alterTableRoot + "ALTER COLUMN " + name + (nullable ? " DROP NOT NULL" : " SET " +
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
                    executeDDL(alterTableRoot + "ALTER COLUMN " + name + " TYPE " + sqlType);
                }
            }
        }

        HashSet<String> leftovers = new HashSet<>(columnsMap.keySet());
        leftovers.removeAll(updatedColumns);

        for (String name : leftovers)
        {
            executeDDL(alterTableRoot + "DROP COLUMN " + name + " CASCADE");
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
            executeDDL("ALTER TABLE " + schemaName + "." + tableName + " DROP CONSTRAINT " + key.name + " " +
                "CASCADE");
        }

    }


    @Override
    public void createPrimaryKey(RuntimeContext runtimeContext, DomainType type)
    {
        final String schemaName = getSchemaName(runtimeContext, type);
        final String tableName = namingStrategy.getTableName(type.getName());

        // add primary key
        executeDDL("ALTER TABLE " + schemaName + "." + tableName + " ADD " + pkConstraint(type));

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

        executeDDL(alterTableRoot + "ADD " + fkConstraint(runtimeContext, targetSchema, type, domainProperty,
            domainProperty.getForeignKey()));

    }


    private String getSchemaName(RuntimeContext runtimeContext, DomainType type)
    {
        final ApplicationConfig configModel = runtimeContext.getApplicationModel().getConfigModel();
        return AppAuthentication.isAuthType(type) ? configModel.getAuthSchema() : configModel.getSchema();
    }


    @Override
    public void renameTable(RuntimeContext runtimeContext, String schema, String from, String to)
    {
        final String tableFrom = namingStrategy.getTableName(from);
        final String tableTo = namingStrategy.getTableName(to);

        executeDDL("ALTER TABLE " + schema + "." + tableFrom + " RENAME TO " + tableTo);
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


    @Override
    public Map<String, DatabaseColumn> listColumns(RuntimeContext runtimeContext, String schemaName, String tableName)
    {

        ColumnMapper columnMapper = new ColumnMapper();
        queryInformationSchema(
            "SELECT * FROM information_schema.columns where table_schema = ? and table_name = ?",
            new Object[]{
                schemaName,
                tableName
            },
            columnMapper
        );
        return columnMapper.getColumnMap();
    }


    @Override
    public void renameField(RuntimeContext runtimeContext, String schema, String type, String from, String to)
    {
        final String tableFrom = namingStrategy.getTableName(type);

        final String[] qualifiedSource = namingStrategy.getFieldName(type, from);
        final String[] qualifiedTarget = namingStrategy.getFieldName(type, to);

        executeDDL(
            "ALTER TABLE " + schema + "." + tableFrom +
                " RENAME COLUMN " + qualifiedSource[1] + " TO " + qualifiedTarget[1]);

    }


    @Override
    public void destroy()
    {
        try
        {
            if (statements != null)
            {
                String path = env.getProperty(ExceedProperties.SCHEMA_DUMP_PREFIX + appName);

                final File outputFile;
                if (path != null)
                {
                    outputFile = new File(path);
                }
                else
                {
                    outputFile = File.createTempFile("exceed-", ".sql");
                }

                FileUtils.writeStringToFile(outputFile, statements.toString(), RequestUtil.UTF_8);
                log.info("Wrote schema statements to {}", outputFile.getPath());
            }
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private List<DatabaseKey> keysMap(String schemaName, String tableName)
    {
        return queryInformationSchema("SELECT DISTINCT constraint_name, column_name, " +
                "position_in_unique_constraint " +
                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE where constraint_schema = ? and table_name = ?;",
            new Object[]{
                schemaName,
                tableName
            },
            (rs, rowNum) -> new DatabaseKey(rs.getString(1), rs.getString(2), rs.getObject(3) != null)
        );
    }

    public <T> List<T> queryInformationSchema(String sql, Object[] params, RowMapper<T> rowMapper)
    {
        if (template == null)
        {
            return Collections.emptyList();
        }

        return template.query(sql, params, rowMapper);

    }

    public void queryInformationSchema(String sql, Object[] params, RowCallbackHandler rowMapper)
    {
        if (template != null)
        {
            template.query(sql, params, rowMapper);
        }
    }

    public <T> List<T> queryInformationSchema(String sql, RowMapper<T> rowMapper)
    {
        if (template == null)
        {
            return Collections.emptyList();
        }

        return template.query(sql, rowMapper);

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
