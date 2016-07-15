package de.quinscape.exceed.model.action;

import de.quinscape.exceed.runtime.domain.DomainObjectBase;
import org.svenson.JSONTypeHint;

import java.util.List;

public class UpdateTranslationActionModel
    extends ActionModel
{
    private List<DomainObjectBase> newTranslations;

    private List<DomainObjectBase> changedTranslations;

    private List<String> removedTranslationIds;


    public List<DomainObjectBase> getNewTranslations()
    {
        return newTranslations;
    }


    @JSONTypeHint(DomainObjectBase.class)
    public void setNewTranslations(List<DomainObjectBase> newTranslations)
    {
        this.newTranslations = newTranslations;
    }


    public List<DomainObjectBase> getChangedTranslations()
    {
        return changedTranslations;
    }


    @JSONTypeHint(DomainObjectBase.class)
    public void setChangedTranslations(List<DomainObjectBase> changedTranslations)
    {
        this.changedTranslations = changedTranslations;
    }


    public List<String> getRemovedTranslationIds()
    {
        return removedTranslationIds;
    }


    public void setRemovedTranslationIds(List<String> removedTranslationIds)
    {
        this.removedTranslationIds = removedTranslationIds;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "newTranslations = " + newTranslations
            + ", changedTranslations = " + changedTranslations
            + ", removedTranslationIds = " + removedTranslationIds
            ;
    }
}


