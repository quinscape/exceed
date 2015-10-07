package de.quinscape.exceed.runtime.domain;

/**
 *  Handles registration of domain types in the system and converts domain objects to JSON and back.
 */
public interface DomainService
{
    <D extends DomainObject> String toJSON(D domainObject);

    DomainObject toDomainObject(String json);

    <D extends DomainObject> D toDomainObject(Class<D> cls, String json);

    DomainRegistry getRegistry();
}
