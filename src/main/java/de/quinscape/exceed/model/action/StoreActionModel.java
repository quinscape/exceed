package de.quinscape.exceed.model.action;

import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import org.svenson.JSONTypeHint;

import java.util.List;

public class StoreActionModel
    extends ActionModel
{
    private GenericDomainObject data;

    private List<GenericDomainObject> list;


    public GenericDomainObject getData()
    {
        return data;
    }


    public void setData(GenericDomainObject data)
    {
        this.data = data;
    }


    public List<GenericDomainObject> getList()
    {
        return list;
    }


    @JSONTypeHint(GenericDomainObject.class)
    public void setList(List<GenericDomainObject> list)
    {
        this.list = list;
    }
}
