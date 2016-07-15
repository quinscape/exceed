package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultTranslator
    implements Translator
{

    private final static Logger log = LoggerFactory.getLogger(DefaultTranslator.class);

    private final TranslationProvider translationProvider;

    private ConcurrentMap<String,TranslationLookup> translationLookup = new ConcurrentHashMap<>();

    public DefaultTranslator(TranslationProvider translationProvider)
    {
        this.translationProvider = translationProvider;
    }

    @Override
    public String translate(RuntimeContext runtimeContext, String code)
    {
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();

        final Map<String, List<AppTranslation>> translations = getTranslationsForApp(runtimeContext);
        final List<AppTranslation> appTranslations = translations.get(code);
        final String currentViewName;
        final String currentProcessName;

        final String locale = applicationModel.matchLocale(runtimeContext.getLocale());


        final View view = runtimeContext.getView();
        if (view != null)
        {
            currentProcessName = view.getProcessName();
            if (currentProcessName != null)
            {
                currentViewName = view.getLocalName();
            }
            else
            {
                currentViewName = view.getName();
            }
        }
        else
        {
            currentViewName = null;
            currentProcessName = null;
        }

        log.debug("locale = {}, currentProcessName = {}, currentViewName = {}", locale, currentProcessName, currentViewName);


        if (appTranslations != null)
        {
            AppTranslation t = findTranslation(appTranslations, locale, currentViewName, currentProcessName);
            if (t != null)
            {
                final String translation = t.getTranslation();
                if (StringUtils.hasText(translation))
                {
                    log.debug("Found translation {}", translation);

                    return translation;
                }
            }
        }

        final int colonPos = code.indexOf(':');
        if (colonPos >= 0)
        {
            code = code.substring(colonPos + 1);
        }

        return "[" + code + "]";
    }


    private Map<String, List<AppTranslation>> getTranslationsForApp(RuntimeContext runtimeContext)
    {
        final String appName = runtimeContext.getApplicationModel().getName();
        final TranslationLookup holder = new TranslationLookup(appName);
        final TranslationLookup existing = translationLookup.putIfAbsent(appName, holder);
        return existing != null ? existing.getTranslations(runtimeContext, translationProvider) : holder.getTranslations(runtimeContext, translationProvider);
    }


    @Override
    public void refreshTranslations(String appName)
    {
        translationLookup.remove(appName);
    }


    private AppTranslation findTranslation(List<AppTranslation> appTranslations, String locale, String currentViewName, String
        currentProcessName)
    {


        for (AppTranslation t : appTranslations)
        {
            final String viewRule = t.getViewName();
            final String processRule = t.getProcessName();

            if (!locale.equals(t.getLocale()))
            {
                continue;
            }

            // if a view rule is defined
            if (viewRule != null)
            {
                // the view name must be equal to the current view name and the process must match the process rule
                // (process Rule equals null meaning "standalone view with that name")
                if (viewRule.equals(currentViewName) && Objects.equals(processRule, currentProcessName))
                {
                    return t;
                }
            }
            // If we have no view rule, match process if we have a process rule
            else if (processRule != null)
            {
                if (processRule.equals(currentProcessName))
                {
                    return t;
                }
            }
            // otherwise just accept the translation we found
            else
            {
                return t;
            }
        }
        return null;
    }
}
