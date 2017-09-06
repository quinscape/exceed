package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;

/**
 * Implemented by classes that synchronize an external storage schema with the current
 * application model.
 */
public interface SchemaService
{
    /**
     * Creates or updates the schema according to the given model.
     *
     */
    void synchronizeSchema(RuntimeContext runtimeContext, List<DomainType> value);

    /**
     * Removes the schema associated with the given application model.
     *
     */
    void removeSchema(RuntimeContext runtimeContext);
}
