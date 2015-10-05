package de.quinscape.exceed.runtime.domain;

import java.util.Map;

public interface DomainTypeRegistry
{
    /**
     * Registers a new domain type with the given name and the given domain type.
     *
     * @param name
     * @param cls
     *
     * @throws DomainTypeNameCollisionException if the name is already registered.
     */
    void register(String name, Class<? extends DomainBase> cls) throws DomainTypeNameCollisionException;

    void override(String name, Class<? extends DomainBase> cls) throws DomainTypeNameNotFoundException;

    /**
     * Provides a read-only view onto the currently registered domain types.
     *
     * @return
     */
    Map<String,Class<? extends DomainBase>> registrations();
}
