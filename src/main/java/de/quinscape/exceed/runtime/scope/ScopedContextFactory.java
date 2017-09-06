package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public class ScopedContextFactory
{
    private final static Logger log = LoggerFactory.getLogger(ScopedContextFactory.class);

    private ApplicationService applicationService;


    /**
     * Autowire lazily with interface based proxy to break a circular reference.
     * 
     * @param applicationService
     */
    @Autowired
    @Lazy
    public void setApplicationService(ApplicationService applicationService)
    {
        this.applicationService = applicationService;
    }


    public final static String SESSION_CONTEXT_ATTRIBUTE_PREFIX = ScopedContextFactory.class + ":sessionContext:";

    private org.springframework.context.ApplicationContext applicationContext;


    public ScopedContextFactory()
    {
    }


    public ApplicationContext createApplicationContext(ContextModel contextModel, String appName)
    {
        if (appName == null)
        {
            throw new IllegalArgumentException("appName can't be null");
        }

        ApplicationContext applicationContext = create(ApplicationContext.class, contextModel);
        applicationContext.setName(appName);
        return applicationContext;
    }

    public SessionContext getSessionContext(HttpServletRequest request, String appName, ContextModel contextModel)
    {
        String key = SESSION_CONTEXT_ATTRIBUTE_PREFIX + appName;
        HttpSession session = request.getSession(true);
        SessionContext sessionContext = (SessionContext) session.getAttribute(key);
        if (sessionContext == null)
        {
            sessionContext = create(SessionContext.class, contextModel);
            session.setAttribute(key, sessionContext);
        }
        return sessionContext;
    }


    public void updateSessionContext(HttpServletRequest request, String appName, SessionContext sessionContext)
    {
        if (sessionContext != null && sessionContext.isDirty())
        {
            String key = SESSION_CONTEXT_ATTRIBUTE_PREFIX + appName;
            HttpSession session = request.getSession(true);
            session.setAttribute(key, sessionContext);
            sessionContext.markClean();
        }
    }


    public ProcessContext createProcessContext(ContextModel contextModel)
    {
        return create(ProcessContext.class, contextModel);
    }


    public void initializeContext(JsEnvironment jsEnvironment, RuntimeContext runtimeContext, ScopedContext scopedContext)
    {
        if (scopedContext.isInitialized())
        {
            return;
        }

        Map<String,Object> input = null;
        if (scopedContext instanceof ApplicationContext)
        {
            String json = applicationService.getApplicationState(((ApplicationContext) scopedContext).getName())
                .getContext();

            //noinspection unchecked
            input = runtimeContext.getDomainService().toDomainObject(Map.class, json);
        }

        scopedContext.init(runtimeContext, jsEnvironment , input);
    }


    private <T extends AbstractScopedContext> T create(Class<T> cls, ContextModel
        contextModel)
    {
        ScopedContext scopedContext;
        if (cls.equals(ApplicationContext.class))
        {
            scopedContext = new ApplicationContext(contextModel);
        }
        else if (cls.equals(SessionContext.class))
        {
            scopedContext = new SessionContext(contextModel);
        }
        else if (cls.equals(ProcessContext.class))
        {
            scopedContext = new ProcessContext(contextModel);
        }
        else
        {
            throw new IllegalArgumentException("Cannot create scope of type " + cls);
        }
        return (T) scopedContext;
    }


    public void updateApplicationContext(String name, ApplicationContext applicationContext, DomainService domainService)
    {
        if (applicationContext != null && applicationContext.isDirty())
        {
            applicationService.updateApplicationContext(name, domainService.toJSON(applicationContext.getContextMap()));
            applicationContext.markClean();
        }
    }


    public ViewContext createViewContext(View view)
    {
        return new ViewContext(view.getContextModel(), view.getName());
    }
}
