package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;

public class SystemStorageOperations
    implements DomainOperations
{
    @Override
    public DataGraph query(RuntimeContext runtimeContext, DomainService domainService, QueryDefinition
        queryDefinition)
    {
        throw new UnsupportedOperationException("System storage does not support query()");
    }


    @Override
    public DomainObject create(RuntimeContext runtimeContext, DomainService domainService, String type, String id, Class<? extends DomainObject>
        implClass)
    {
        final GenericDomainObject genericDomainObject = new GenericDomainObject();
        genericDomainObject.setDomainType(type);
        genericDomainObject.setDomainService(domainService);
        genericDomainObject.setId(id);
        return genericDomainObject;
    }


    @Override
    public DomainObject read(RuntimeContext runtimeContext, DomainService domainService, String type, String id)
    {
        throw new UnsupportedOperationException("System storage does not support read()");
    }


    @Override
    public boolean delete(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
    {
        throw new UnsupportedOperationException("System storage does not support delete()");
    }


    @Override
    public void insert(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
    {
        throw new UnsupportedOperationException("System storage does not support insert()");

    }


    @Override
    public void insertOrUpdate(RuntimeContext
                                   runtimeContext, DomainService domainService, DomainObject genericDomainObject)
    {
        throw new UnsupportedOperationException("System storage does not support insertOrUpdate()");
    }


    @Override
    public boolean update(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
    {
        throw new UnsupportedOperationException("System storage does not support update()");
    }
}
