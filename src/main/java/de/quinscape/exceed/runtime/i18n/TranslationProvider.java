package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;

public interface TranslationProvider
{
    List<AppTranslation> provideTranslations(RuntimeContext runtimeContext);
}
