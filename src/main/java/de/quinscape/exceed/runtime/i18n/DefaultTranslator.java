package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.runtime.RuntimeContext;

public class DefaultTranslator
    implements Translator
{
    @Override
    public String translate(RuntimeContext runtimeContext, String code)
    {
        return "[" + code + "]";
    }
}
