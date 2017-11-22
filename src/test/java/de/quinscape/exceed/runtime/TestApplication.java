package de.quinscape.exceed.runtime;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.i18n.DefaultTranslator;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.scope.ApplicationContext;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.SessionContext;
import de.quinscape.exceed.runtime.scope.ViewContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TestApplication
    implements RuntimeApplication
{
    private final ApplicationModel applicationModel;

    private final DomainService domainService;

    public TestApplication(ApplicationModel applicationModel, DomainService domainService)
    {
        this.applicationModel = applicationModel;
        this.domainService = domainService;

        this.domainService.init(this, null);
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


    @Override
    public RuntimeContext createSystemContext()
    {
        return null;
    }


    public RuntimeContext createRuntimeContext()
    {
        return createRuntimeContext(null, null);
    }
    public RuntimeContext createRuntimeContext(View view)
    {
        return createRuntimeContext(view, null);
    }

    public RuntimeContext createRuntimeContext(View view, Map<String, Object> processInput)
    {
        return createRuntimeContext(view, null, processInput);
    }

    public RuntimeContext createRuntimeContext(ProcessState processState)
    {
        return createRuntimeContext(null , processState, Collections.emptyMap());
    }

    private RuntimeContext createRuntimeContext(View view, ProcessState processState, Map<String, Object> processInput)
    {
        final ScopedContext viewContext = view != null ? new ViewContext(view.getContextModel(), view.getName()) :
            null;
        final ContextModel applicationContextModel = applicationModel.getConfigModel().getApplicationContextModel();
        final ContextModel sessionContextModel = applicationModel.getConfigModel().getSessionContextModel();

        final ApplicationContext applicationContext = applicationContextModel != null ? new ApplicationContext(applicationContextModel) : null;
        final SessionContext sessionContext = sessionContextModel != null ? new SessionContext(sessionContextModel) : null;

        final RuntimeContext runtimeContext = new RuntimeContext(
            this,
            "/test",
            new DefaultTranslator(runtimeContext1 -> Collections.emptyList()),
            Locale.getDefault(),
            new ScopedContextChain(
                Arrays.asList(
                    viewContext,
                    null,
                    sessionContext,
                    applicationContext
                ),
                applicationModel.getMetaData().getScopeMetaModel(),
                view != null ? view.getScopeLocation() : null
            ), domainService
        );

        runtimeContext.setView(view);
        RuntimeContextHolder.register(runtimeContext, null);

        final JsEnvironment jsEnvironment = applicationModel.getMetaData().getJsEnvironment();


        final Process process;
        final String scopeLocation;

        if (view != null && view.getProcessName() != null)
        {
            process = applicationModel.getProcess(view.getProcessName());
            scopeLocation = view.getScopeLocation();
        }
        else if (processState != null)
        {
            process = processState.getProcess();
            scopeLocation = processState.getScopeLocation();
        }
        else
        {
            process = null;
            scopeLocation = null;
        }

        if (process != null)
        {

            final ProcessContext processContext = new ProcessContext(process.getContextModel());
            processContext.init(runtimeContext, jsEnvironment, processInput);
            runtimeContext.getScopedContextChain().update(processContext, scopeLocation);
        }

        if (viewContext != null)
        {
            viewContext.init(runtimeContext, jsEnvironment, new HashMap<>());
        }
        if (sessionContext != null)
        {
            sessionContext.init(runtimeContext, jsEnvironment, new HashMap<>());
        }
        if (applicationContext != null)
        {
            applicationContext.init(runtimeContext, jsEnvironment, new HashMap<>());
        }

        return runtimeContext;
    }


    public DomainService getDomainService()
    {
        return domainService;
    }
}
