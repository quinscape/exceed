package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;

import java.util.Map;
import java.util.Set;

/**
 *  Handles registration of domain types in the system and converts domain objects to JSON and back.
 */
public interface DomainService
{
    void init(RuntimeApplication runtimeApplication, String schema);

    String toJSON(Object domainObject);

    <T> T toDomainObject(Class<T> cls, String json);

    DomainType getDomainType(String name);

    String getSchema();

    Set<String> getDomainTypeNames();

    Map<String,EnumType> getEnums();

    DomainObject create(String type, String id);

    DomainObject read(String type, String id);

    void delete(DomainObject genericDomainObject);

    void insert(DomainObject genericDomainObject);

    void update(DomainObject genericDomainObject);

    PropertyConverter getPropertyConverter(String name);

    NamingStrategy getNamingStrategy();
}
