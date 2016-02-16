package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;

import java.util.Map;
import java.util.Set;

/**
 *  Handles registration of domain types in the system and converts domain objects to JSON and back.
 */
public interface DomainService
{
    void init(RuntimeApplication runtimeApplication, String schema);

    String toJSON(Object domainObject);

    Object toDomainObject(String json);

    DomainType getDomainType(String name);

    String getSchema();

    Set<String> getDomainTypeNames();

    Map<String,EnumModel> getEnums();
}
