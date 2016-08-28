package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;

public interface ScopedResolver
{
    Object getProperty(String name);

    ScopedPropertyModel getModel(String name);

    void setProperty(String name, Object value);

    boolean hasProperty(String name);
}
