package de.quinscape.exceed;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;

import java.util.Collections;
import java.util.Map;

public class TestDomainServiceBase
    implements DomainService
{
    @Override
    public void init(RuntimeApplication runtimeApplication, String schema)
    {
        
    }


    @Override
    public String toJSON(Object domainObject)
    {
        return null;
    }


    @Override
    public <T> T toDomainObject(Class<T> cls, String json)
    {
        return null;
    }


    @Override
    public DomainType getDomainType(String name)
    {
        return getDomainTypes().get(name);
    }


    @Override
    public String getSchema()
    {
        return "test";
    }


    @Override
    public Map<String, DomainType> getDomainTypes()
    {
        return Collections.emptyMap();
    }


    @Override
    public Map<String, EnumType> getEnums()
    {
        return Collections.emptyMap();
    }


    @Override
    public DomainObject create(String type, String id)
    {
        return null;
    }


    @Override
    public DomainObject read(String type, String id)
    {
        return null;
    }


    @Override
    public void delete(DomainObject genericDomainObject)
    {

    }


    @Override
    public void insert(DomainObject genericDomainObject)
    {

    }


    @Override
    public void insertOrUpdate(DomainObject genericDomainObject)
    {

    }


    @Override
    public void update(DomainObject genericDomainObject)
    {

    }


    @Override
    public PropertyConverter getPropertyConverter(String name)
    {
        return null;
    }


    @Override
    public StorageConfiguration getStorageConfiguration(String domainType)
    {
        return null;
    }
}
