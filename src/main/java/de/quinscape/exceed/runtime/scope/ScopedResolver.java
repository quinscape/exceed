package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.DomainObject;

public interface ScopedResolver
{
    Object getProperty(String name);

    DomainObject getObject(String name);

    DataList getList(String name);

    void setProperty(String name, Object value);

    void setObject(String name, DomainObject value);

    void setList(String name, DataList list);

    boolean hasProperty(String name);

    boolean hasObject(String name);

    boolean hasList(String name);
}
