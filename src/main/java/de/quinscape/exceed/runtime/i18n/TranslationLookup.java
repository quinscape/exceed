package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationLookup
{
    private final String app;

    private volatile Map<String, List<AppTranslation>> map;


    public TranslationLookup(String app)
    {
        this.app = app;
    }

    public Map<String,List<AppTranslation>> getTranslations(RuntimeContext runtimeContext, TranslationProvider loader)
    {
        if (map == null)
        {
            synchronized (this)
            {
                if (map == null)
                {
                    map = createMap(loader.provideTranslations(runtimeContext));
                }
            }
        }

        return map;
    }


    private Map<String, List<AppTranslation>> createMap(List<AppTranslation> translations)
    {
        Map<String, List<AppTranslation>> map = new HashMap<>();
        for (AppTranslation translation : translations)
        {
            final String tag = translation.getTag();

            List<AppTranslation> appTranslations = map.get(tag);
            if (appTranslations == null)
            {
                appTranslations = new ArrayList<>();
                map.put(tag, appTranslations);
            }
            appTranslations.add(translation);
        }

        for (List<AppTranslation> appTranslations : map.values())
        {
            // make sure the array is in order of decreasing specificity
            Collections.sort(appTranslations, TranslationSorter.INSTANCE);
        }

        return map;

    }

}
