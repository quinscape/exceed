package de.quinscape.exceed.runtime.domain;

/**
 * Implemented by classes providing basic CRUD operations for a domain type.
 */
public interface DomainOperations
{
    /**
     * Creates a new instance of the given type
     * @param domainService     domain service to use.
     * @param type              type known to the given domain service
     * @param id                initial id
     * @return new instance
     */
    DomainObject create(DomainService domainService, String type, String id);

    /**
     * Reads the domain object with the given id from storage.
     *
     * @param domainService     domain service to use.
     * @param type              type known to the given domain service
     * @param id                id of the domain object to be read
     * @return  domain object
     */
    DomainObject read(DomainService domainService, String type, String id);


    /**
     * Deletes the given domain object from storage.
     *
     * @param domainService         domain service to use.
     * @param genericDomainObject   domain object
     */
    void delete(DomainService domainService, DomainObject genericDomainObject);

    /**
     * Inserts the given domain object into storage.
     *
     * @param domainService         domain service to use.
     * @param genericDomainObject   domain object
     */
    void insert(DomainService domainService, DomainObject genericDomainObject);

    /**
     * Inserts or updates the given domain object into storage, depending on whether it
     * already exists, or not.
     *
     * @param domainService         domain service to use.
     * @param genericDomainObject   domain object
     */
    void insertOrUpdate(DomainService domainService, DomainObject genericDomainObject);

    /**
     * Updates the given domain object in storage.
     *
     * @param genericDomainObject   domain object
     * @param domainService         domain service to use.
     */
    void update(DomainService domainService, DomainObject genericDomainObject);
}
