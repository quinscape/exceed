package de.quinscape.exceed.runtime.domain;

import org.svenson.JSONProperty;

import java.util.Set;

public interface DomainObject
{
    String getId();

    void setId(String id);

    DomainService getDomainService();

    void setDomainService(DomainService domainService);

    String getType();

    Set<String> propertyNames();

    Object getProperty(String name);

    void setProperty(String name, Object value);

    default void update()
    {
        getDomainService().update(this);
    }

    default void insert()
    {
        getDomainService().insert(this);
    }

    default void delete()
    {
        getDomainService().delete(this);
    }
}
