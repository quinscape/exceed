package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.component.QueryResult;

/**
 *  Handles registration of domain types in the system and converts domain objects to JSON and back.
 */
public interface DomainService
{
    void init(RuntimeApplication runtimeApplication, String schema);

    String toJSON(RuntimeContext runtimeContext, Object domainObject);

    Object toDomainObject(RuntimeContext runtimeContext, String json);

    DomainType getDomainType(String name);

    String getSchema();
}
