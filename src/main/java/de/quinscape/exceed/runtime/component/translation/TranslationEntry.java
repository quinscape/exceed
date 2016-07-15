package de.quinscape.exceed.runtime.component.translation;


import de.quinscape.exceed.runtime.domain.DomainObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationEntry
{
    private Map<String, DomainObject> translations = new HashMap<>();

    private List<DomainObject> localTranslations = new ArrayList<>();

    private List<ReferenceInfo> references = new ArrayList<>();

    private String name;


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public Map<String, DomainObject> getTranslations()
    {
        return translations;
    }


    public void setTranslations(Map<String, DomainObject> translations)
    {
        this.translations = translations;
    }


    public List<DomainObject> getLocalTranslations()
    {
        return localTranslations;
    }


    public void setLocalTranslations(List<DomainObject> localTranslations)
    {
        this.localTranslations = localTranslations;
    }


    public void addReference(ReferenceInfo referenceInfo)
    {
        this.references.add(referenceInfo);
    }


    public List<ReferenceInfo> getReferences()
    {
        return references;
    }
}
