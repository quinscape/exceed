package de.quinscape.exceed.runtime.component.translation;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import org.svenson.JSONTypeHint;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TranslationEditorState
{
    private Map<String,TranslationEntry> translations = new HashMap<>();


    public Map<String, TranslationEntry> getTranslations()
    {
        return translations;
    }


    @JSONTypeHint(TranslationEntry.class)
    public void addTranslations(DomainObject translation)
    {
        final String tag = (String) translation.getProperty("tag");
        final String locale = (String) translation.getProperty("locale");
        final String processName = (String) translation.getProperty("processName");
        final String viewName = (String) translation.getProperty("viewName");

        TranslationEntry entry = entry(tag);

        if (processName == null && viewName == null)
        {
            entry.getTranslations().put(locale, translation);
        }
        else
        {
            entry.getLocalTranslations().add(translation);
        }
    }

    public Set<String> getTagNames()
    {
        return translations.keySet();
    }

    public TranslationEntry entry(String tag)
    {
        TranslationEntry entry = translations.get(tag);
        if (entry == null)
        {
            entry = new TranslationEntry();
            entry.setName(tag);
            translations.put(tag, entry);
        }
        return entry;
    }


    public void setTranslations(Map<String, TranslationEntry> translations)
    {
        this.translations = translations;
    }


    public void addReference(String tag, ReferenceType type, String name)
    {
        TranslationEntry entry = entry(tag);

        entry.addReference(new ReferenceInfo(type, name));
    }


    public Set<String> initializeDefaults(RuntimeContext runtimeContext, List<String> supportedLocales)
    {
        final DomainService domainService = runtimeContext.getDomainService();

        final HashSet<String> newIds = new HashSet<>();
        for (TranslationEntry translationEntry : translations.values())
        {
            for (String locale : supportedLocales)
            {
                final Map<String, DomainObject> translations = translationEntry.getTranslations();
                if (translations.get(locale) == null)
                {
                    final String id = UUID.randomUUID().toString();
                    final DomainObject domainObject = domainService.create("AppTranslation", id);

                    domainObject.setProperty("tag", translationEntry.getName());

                    // save conversion by converting "created", everything else does not need conversion
                    final PropertyConverter timestamp = domainService.getPropertyConverter("Timestamp");

                    domainObject.setProperty("created", timestamp.convertToJSON(runtimeContext, new Timestamp(System.currentTimeMillis())));
                    domainObject.setProperty("locale", locale);
                    domainObject.setProperty("translation", "");
                    domainObject.setProperty("processName", null);
                    domainObject.setProperty("viewName", null);

                    translations.put(locale, domainObject);
                    newIds.add(id);
                }
            }
        }
        return newIds;
    }
}
