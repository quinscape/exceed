package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.runtime.domain.DomainOperations;
import de.quinscape.exceed.runtime.domain.NamingStrategy;

/**
 * Contains a number of interface implementations that together handle the schema synchronization, data querying
 *
 *
 */
public class DefaultStorageConfiguration
    implements StorageConfiguration
{
    private final DomainOperations domainOperations;

    private final NamingStrategy namingStrategy;

    private final SchemaService schemaService;

    public DefaultStorageConfiguration(DomainOperations domainOperations, NamingStrategy namingStrategy, SchemaService schemaService)
    {
        if (namingStrategy == null)
        {
            throw new IllegalArgumentException("namingStrategy can't be null");
        }

        this.domainOperations = domainOperations;
        this.namingStrategy = namingStrategy;
        this.schemaService = schemaService;
    }


    @Override
    public NamingStrategy getNamingStrategy()
    {
        return namingStrategy;
    }


    @Override
    public DomainOperations getDomainOperations()
    {
        return domainOperations;
    }


    @Override
    public SchemaService getSchemaService()
    {
        return schemaService;
    }
}
