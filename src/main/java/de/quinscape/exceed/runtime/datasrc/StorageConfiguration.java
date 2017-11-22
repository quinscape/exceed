package de.quinscape.exceed.runtime.datasrc;

import de.quinscape.exceed.runtime.domain.DomainOperations;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.schema.SchemaService;

public final class StorageConfiguration
{
    private final NamingStrategy namingStrategy;

    private final DomainOperations domainOperations;

    private final SchemaService schemaService;


    public StorageConfiguration(
        NamingStrategy namingStrategy, DomainOperations domainOperations,
        SchemaService schemaService
    )
    {
        this.namingStrategy = namingStrategy;
        this.domainOperations = domainOperations;
        this.schemaService = schemaService;
    }


    public NamingStrategy getNamingStrategy()
    {
        return namingStrategy;
    }


    public DomainOperations getDomainOperations()
    {
        return domainOperations;
    }


    public SchemaService getSchemaService()
    {
        return schemaService;
    }


    public StorageConfiguration merge(StorageConfiguration other)
    {
        final NamingStrategy namingStrategy;

        if (other.namingStrategy != null)
        {
            namingStrategy = other.namingStrategy;
        }
        else
        {
            namingStrategy = this.namingStrategy;
        }

        final DomainOperations domainOperations;
        if (other.domainOperations != null)
        {
            domainOperations = other.domainOperations;
        }
        else
        {
            domainOperations = this.domainOperations;
        }

        final SchemaService schemaService;
        if (other.schemaService != null)
        {
            schemaService = other.schemaService;
        }
        else
        {
            schemaService = this.schemaService;
        }

        return new StorageConfiguration(
            namingStrategy,
            domainOperations,
            schemaService
        );
    }
}
