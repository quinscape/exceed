package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.runtime.RuntimeContext;

public interface Translator
{
    String I18N_CALL_NAME = "i18n";

    String translate(RuntimeContext runtimeContext, String code);

    void refreshTranslations(String appName);
}
