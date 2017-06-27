package de.quinscape.exceed.model.translation;


import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.annotation.ExceedPropertyType;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.domain.DomainObject;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the data associated with a single translation key within the translation editor.
 *
 */
public class TranslationEntry
    extends Model
{
    /**
     * Maps a locale code to a AppTranslation domain object.
     */
    private Map<String, DomainObject> translations = new HashMap<>();

    /**
     * Contains local rules for the translation key
     */
    private List<DomainObject> localTranslations = new ArrayList<>();

    private List<ReferenceInfo> references = new ArrayList<>();

    /** Translation key name */
    private String name;


    public TranslationEntry(String name)
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }


    public Map<String, DomainObject> getTranslations()
    {
        return translations;
    }


    @ExceedPropertyType(type = DomainProperty.MAP_PROPERTY_TYPE, typeParam = "AppTranslation")
    public void setTranslations(Map<String, DomainObject> translations)
    {
        this.translations = translations;
    }


    public List<DomainObject> getLocalTranslations()
    {
        return localTranslations;
    }


    @ExceedPropertyType(type = DomainProperty.LIST_PROPERTY_TYPE, typeParam = "AppTranslation")
    public void setLocalTranslations(List<DomainObject> localTranslations)
    {
        this.localTranslations = localTranslations;
    }


    @JSONProperty(ignore = true)
    public void addReference(ReferenceInfo referenceInfo)
    {
        this.references.add(referenceInfo);
    }


    @JSONTypeHint(ReferenceInfo.class)
    public List<ReferenceInfo> getReferences()
    {
        return references;
    }
}
