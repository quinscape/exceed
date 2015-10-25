package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.runtime.RuntimeContext;

public interface Translator
{
    String translate(RuntimeContext runtimeContext, String code);
}
