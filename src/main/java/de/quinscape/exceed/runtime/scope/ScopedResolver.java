package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;

public interface ScopedResolver
{
    Object getProperty(String name);

    DomainObject getObject(String name);

    DataGraph getList(String name);

    void setProperty(String name, Object value);

    void setObject(String name, DomainObject value);

    void setList(String name, DataGraph list);

    boolean hasProperty(String name);

    boolean hasObject(String name);

    boolean hasList(String name);
}
