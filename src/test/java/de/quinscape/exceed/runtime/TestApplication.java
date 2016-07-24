package de.quinscape.exceed.runtime;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.i18n.DefaultTranslator;
import de.quinscape.exceed.runtime.i18n.JOOQTranslationProvider;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;

import java.util.Collections;
import java.util.Locale;

public class TestApplication
    implements RuntimeApplication
{
    private final ApplicationModel applicationModel;

    private final DomainService domainService;


    public TestApplication(ApplicationModel applicationModel, DomainService domainService)
    {
        this.applicationModel = applicationModel;
        this.domainService = domainService;

        this.domainService.init(this, "test");
    }




    @Override
    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }


    @Override
    public ScopedContext getApplicationContext()
    {
        return null;
    }


    @Override
    public ResourceLoader getResourceLoader()
    {
        return null;
    }


    public RuntimeContext createRuntimeContext()
    {
        return new RuntimeContext(this, "/test", new DefaultTranslator(null), Locale.getDefault(), new ScopedContextChain(Collections.emptyList()), domainService);
    }
}
