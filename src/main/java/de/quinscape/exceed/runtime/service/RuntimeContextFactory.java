package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;

import java.util.Locale;

public class RuntimeContextFactory
{
    private final Translator translator;


    public RuntimeContextFactory(Translator translator)
    {
        this.translator = translator;
    }


    public RuntimeContext create(RuntimeApplication runtimeApplication, String path, Locale locale, ScopedContextChain scopedContextChain, DomainService domainService)
    {
        return new RuntimeContext(runtimeApplication, path, translator, locale, scopedContextChain, domainService);
    }
}
