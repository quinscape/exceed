package de.quinscape.exceed.runtime.domain;

import java.util.Map;

public interface DomainRegistry
{
    /**
     * Registers a new domain type with the given name and the given domain type.
     *
     * @param name
     * @param cls
     *
     * @throws DomainTypeNameCollisionException if the name is already registered.
     */
    void register(String name, Class<? extends DomainObject> cls) throws DomainTypeNameCollisionException;

    /**
     * Overrides the domain type with the given name with a sub class of the same type.
     *
     * @param name                                  existing domain type name
     * @param cls                                   sub type of existing domain type

     * @throws DomainTypeNameNotFoundException      if no type with the given name was found
     * @throws DomainTypeOverridingException        if the type with the given name is not a valid super class
     */
    void override(String name, Class<? extends DomainObject> cls) throws DomainTypeNameNotFoundException, DomainTypeOverridingException;

    /**
     * Provides a read-only view onto the currently registered domain types.
     *
     * @return
     */
    Map<String,Class<? extends DomainObject>> registrations();
}
