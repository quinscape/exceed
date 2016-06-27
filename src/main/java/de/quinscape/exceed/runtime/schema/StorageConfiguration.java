package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.runtime.component.QueryExecutor;
import de.quinscape.exceed.runtime.domain.DomainOperations;
import de.quinscape.exceed.runtime.domain.NamingStrategy;

/**
 * Encapsulates storage configuration for a domain type
 */
public interface StorageConfiguration
{
    /**
     * The naming strategy to be used for the domain type.
     * @return  naming strategy
     */
    NamingStrategy getNamingStrategy();

    /**
     * Returns the query executor for the domain type.
     *
     * @return query executor implementation
     */
    QueryExecutor getQueryExecutor();

    /**
     * Returns the domain operations implementation for the domain type.
     *
     * @return   domain operations implementation
     */
    DomainOperations getDomainOperations();

    /**
     * Returns the schema config to be used for the domain type.
     *
     * @return schema config bean name
     */
    SchemaService getSchemaService();
}
