package de.quinscape.exceed.model.action;

import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.List;

public class UpdateDomainActionModel
    extends ActionModel
{
    private List<DomainType> newTypes;

    private List<Integer> newTypeExtensions;

    private List<DomainType> changedTypes;

    private List<DomainType> removedTypes;


    public List<DomainType> getNewTypes()
    {
        if (newTypes == null)
        {
            return Collections.emptyList();
        }

        return newTypes;
    }


    @JSONTypeHint(DomainTypeModel.class)
    public void setNewTypes(List<DomainType> newTypes)
    {
        this.newTypes = newTypes;
    }


    public List<DomainType> getChangedTypes()
    {
        if (changedTypes == null)
        {
            return Collections.emptyList();
        }

        return changedTypes;
    }


    @JSONTypeHint(DomainTypeModel.class)
    public void setChangedTypes(List<DomainType> changedTypes)
    {
        this.changedTypes = changedTypes;
    }


    public List<DomainType> getRemovedTypes()
    {
        if (removedTypes == null)
        {
            return Collections.emptyList();
        }

        return removedTypes;
    }


    @JSONTypeHint(DomainTypeModel.class)
    public void setRemovedTypes(List<DomainType> removedTypes)
    {
        this.removedTypes = removedTypes;
    }


    public List<Integer> getNewTypeExtensions()
    {
        return newTypeExtensions;
    }


    public void setNewTypeExtensions(List<Integer> newTypeExtensions)
    {
        this.newTypeExtensions = newTypeExtensions;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "newTypes = " + newTypes
            + ", newTypeExtensions = " + newTypeExtensions
            + ", changedTypes = " + changedTypes
            + ", removedTypes = " + removedTypes
            ;
    }
}


