package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RuntimeContextFactory
{
    @Autowired
    private Translator translator;

    public RuntimeContext create(RuntimeApplication runtimeApplication, String path, Locale locale, ScopedContextChain scopedContextChain)
    {
        return new RuntimeContext(runtimeApplication, path, translator, locale, scopedContextChain);
    }
}
