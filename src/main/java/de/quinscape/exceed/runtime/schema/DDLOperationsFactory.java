package de.quinscape.exceed.runtime.schema;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;

/**
 * Creates DDLOperations instances.
 */
public interface DDLOperationsFactory
{
    DDLOperations create(RuntimeContext runtimeContext, ExceedDataSource dataSource);
}
