package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;

public interface TranslationProvider
{
    String TRANSLATION_TYPE = "AppTranslation";
    
    String PROPERTY_ID = "id";
    String PROPERTY_LOCALE = "locale";
    String PROPERTY_TAG = "tag";
    String PROPERTY_TRANSLATION = "translation";
    String PROPERTY_CREATED = "created";
    String PROPERTY_PROCESS_NAME = "processName";
    String PROPERTY_VIEW_NAME = "viewName";

    List<AppTranslation> provideTranslations(RuntimeContext runtimeContext);
}
