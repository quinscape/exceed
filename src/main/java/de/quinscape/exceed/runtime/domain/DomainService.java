package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumModel;
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

    Object toDomainObject(Class<?> cls, String json);

    DomainType getDomainType(String name);

    String getSchema();

    Set<String> getDomainTypeNames();

    Map<String,EnumModel> getEnums();

    GenericDomainObject read(String type, Object... pkFields);

    default GenericDomainObject read(String type, String id)
    {
        return read(type, (Object)id);
    }

    void delete(DomainObject genericDomainObject);

    void insert(DomainObject genericDomainObject);

    void update(DomainObject genericDomainObject);

    NamingStrategy getNamingStrategy();
}
