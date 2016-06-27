package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;

/**
 * Implemented by classes that provide data definition operations as used by the {@link DefaultSchemaService}.
 */
public interface DDLOperations
{
    /**
     * Lists the existing schemata.
     *
     * @return list of schemata
     */
    List<String> listSchemata();

    /**
     * Deletes a schema.
     *
     * @param name      schema name
     */
    void dropSchema(String name);

    /**
     * Creates a schema.
     *
     * @param name      schema name
     */
    void createSchema(String name);

    /**
     * Lists all existing tables of a given schema
     *
     * @param schema    schema name
     *
     * @return list of tables
     */
    List<String> listTables(String schema);

    /**
     * Creates a new database table for the given domain type.
     *
     * @param runtimeContext    runtime context
     * @param type              domain type
     */
    void createTable(RuntimeContext runtimeContext, DomainType type);

    /**
     * Updates the table for the given domain type.
     *
     * @param runtimeContext    runtime context
     * @param type              domain type
     */
    void updateTable(RuntimeContext runtimeContext, DomainType type);

    /**
     * Drops all keys for the given domain type / table.
     *
     * @param runtimeContext    runtime context
     * @param type              domain type
     */
    void dropKeys(RuntimeContext runtimeContext, DomainType type);

    /**
     * Creates the primary key for the given domain type / table.
     *
     * @param runtimeContext    runtime context
     * @param type              domain type
     */
    void createPrimaryKey(RuntimeContext runtimeContext, DomainType type);

    /**
     * Creates a foreign keys for the given domain type / table and domain property / field.
     *
     *  @param runtimeContext        runtime context
     *  @param type                  domain type
     *  @param domainProperty        domain property
     */
    void createForeignKeys(RuntimeContext runtimeContext, DomainType type, DomainProperty domainProperty);

}
