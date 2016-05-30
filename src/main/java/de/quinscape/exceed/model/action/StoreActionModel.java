package de.quinscape.exceed.model.action;

import de.quinscape.exceed.runtime.domain.DomainObjectBase;

public class StoreActionModel
    extends ActionModel
{
    private DomainObjectBase object;


    public DomainObjectBase getObject()
    {
        return object;
    }


    public void setObject(DomainObjectBase object)
    {
        this.object = object;
    }
}
