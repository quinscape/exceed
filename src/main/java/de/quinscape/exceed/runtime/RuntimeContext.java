package de.quinscape.exceed.runtime;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.i18n.Translator;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Contains information about the current request and the context in which it happens.
 *
 * @see RuntimeContextHolder#get()
 */
public class RuntimeContext
{
    private final String path;

    private final RuntimeApplication runtimeApplication;

    private final Locale locale;

    private final Translator translator;

    private final ScopedContextChain scopedContextChain;

    private final DomainService domainService;

    private View view;

    private Map<String, Object> variables;

    private String routingTemplate;

    public RuntimeContext(RuntimeApplication runtimeApplication,
                          String path, Translator translator, Locale locale, ScopedContextChain scopedContextChain,
                          DomainService domainService)
    {
        this.path = path;
        this.runtimeApplication = runtimeApplication;
        this.translator = translator;
        this.locale = locale;
        this.domainService = domainService;
        this.scopedContextChain = scopedContextChain != null ? scopedContextChain : new ScopedContextChain(Collections.emptyList(), null, null);
    }



    public String getPath()
    {
        return path;
    }


    public RuntimeApplication getRuntimeApplication()
    {
        return runtimeApplication;
    }


    public Locale getLocale()
    {
        return locale;
    }


    public Translator getTranslator()
    {
        return translator;
    }


    public void setView(View view)
    {
        this.view = view;
    }


    public View getView()
    {
        return view;
    }


    public void setVariables(Map<String, Object> variables)
    {
        this.variables = variables;
    }


    public String getRoutingTemplate()
    {
        return routingTemplate;
    }


    public void setRoutingTemplate(String routingTemplate)
    {
        this.routingTemplate = routingTemplate;
    }


    /**
     * Returns the location params which consist of the path variables and the HTTP parameters received for the
     * current mapping.
     *
     * @return
     */
    public Map<String, Object> getLocationParams()
    {
        return variables;
    }


    /**
     * Returns the scoped context chain for this runtime context containing the application and session context for the user and
     * also a process context if current execution is within a process.
     *
     * @return
     */
    public ScopedContextChain getScopedContextChain()
    {
        return scopedContextChain;
    }


    public void setVariable(String stateId, String id)
    {
        this.variables.put(stateId, id);
    }


    public DomainService getDomainService()
    {
        return domainService;
    }


    public ApplicationModel getApplicationModel()
    {
        return runtimeApplication.getApplicationModel();
    }


}
