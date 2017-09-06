package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;

/**
 * Implemented by classes providing basic CRUD operations for a domain type.
 */
public interface DomainOperations
{

    DataGraph query(RuntimeContext runtimeContext, DomainService domainService, QueryDefinition queryDefinition);

    /**
     * Creates a new instance of the given type
     *
     * @param runtimeContext
     * @param domainService     domain service to use.
     * @param type              type known to the given domain service
     * @param id                initial id
     * @param implClass
     * @return new instance
     */
    DomainObject create(RuntimeContext runtimeContext, DomainService domainService, String type, String id, Class<? extends DomainObject> implClass);

    /**
     * Reads the domain object with the given id from storage.
     *
     *
     * @param runtimeContext
     * @param domainService     domain service to use.
     * @param type              type known to the given domain service
     * @param id                id of the domain object to be read
     * @return  domain object
     */
    DomainObject read(RuntimeContext runtimeContext, DomainService domainService, String type, String id);


    /**
     * Deletes the given domain object from storage.
     *  @param runtimeContext
     * @param domainService         domain service to use.
     * @param genericDomainObject   domain object
     */
    boolean delete(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject);

    /**
     * Inserts the given domain object into storage.
     *
     * @param runtimeContext
     * @param domainService         domain service to use.
     * @param genericDomainObject   domain object
     */
    void insert(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject);

    /**
     * Inserts or updates the given domain object into storage, depending on whether it
     * already exists, or not.
     * @param runtimeContext
     * @param domainService         domain service to use.
     * @param genericDomainObject   domain object
     */
    void insertOrUpdate(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject);

    /**
     * Updates the given domain object in storage.
     *  @param runtimeContext
     * @param domainService         domain service to use.
     * @param genericDomainObject   domain object
     */
    boolean update(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject);
}
