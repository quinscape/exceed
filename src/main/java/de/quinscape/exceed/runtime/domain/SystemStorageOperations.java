package de.quinscape.exceed.runtime.domain;

public class SystemStorageOperations
    implements DomainOperations
{
    @Override
    public DomainObject create(DomainService domainService, String type, String id)
    {
        final GenericDomainObject genericDomainObject = new GenericDomainObject();
        genericDomainObject.setDomainType(type);
        genericDomainObject.setDomainService(domainService);
        genericDomainObject.setId(id);
        return genericDomainObject;
    }


    @Override
    public DomainObject read(DomainService domainService, String type, String id)
    {
        throw new UnsupportedOperationException("System storage does not support read()");
    }


    @Override
    public void delete(DomainService domainService, DomainObject genericDomainObject)
    {
        throw new UnsupportedOperationException("System storage does not support delete()");
    }


    @Override
    public void insert(DomainService domainService, DomainObject genericDomainObject)
    {
        throw new UnsupportedOperationException("System storage does not support insert()");

    }


    @Override
    public void insertOrUpdate(DomainService domainService, DomainObject genericDomainObject)
    {
        throw new UnsupportedOperationException("System storage does not support insertOrUpdate()");
    }


    @Override
    public void update(DomainService domainService, DomainObject genericDomainObject)
    {
        throw new UnsupportedOperationException("System storage does not support update()");
    }
}
