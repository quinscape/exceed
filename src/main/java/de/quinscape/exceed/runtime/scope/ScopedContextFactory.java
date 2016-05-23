package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public class ScopedContextFactory
{
    private final static Logger log = LoggerFactory.getLogger(ScopedContextFactory.class);

    private final ApplicationService applicationService;

    private final ExpressionService expressionService;

    private final ActionService actionService;


    public final static String SESSION_CONTEXT_ATTRIBUTE_PREFIX = ScopedContextFactory.class + ":sessionContext:";


    public ScopedContextFactory(ApplicationService applicationService, ExpressionService expressionService,
                                ActionService actionService)
    {
        this.applicationService = applicationService;
        this.expressionService = expressionService;
        this.actionService = actionService;
    }


    public ApplicationContext createApplicationContext(ContextModel contextModel, String appName, DomainService
        domainService)
    {
        if (appName == null)
        {
            throw new IllegalArgumentException("appName can't be null");
        }

        if (domainService == null)
        {
            throw new IllegalArgumentException("domainService can't be null");
        }


        AppState state = applicationService.getApplicationState(appName);
        String json;
        if (state != null && (json = state.getContext()) != null)
        {
            // restore from DB
            try
            {
                log.info("Using application context from DB: {}", json);

                Map<String, Object> context = (Map<String, Object>) domainService.toDomainObject(Map.class, json);
                return new ApplicationContext(contextModel, context);
            }
            catch(Exception e)
            {
                // if we can't restore the context from DB, we just warn and create a new warn
                log.warn("Could not restore application context from DB, creating default", e);
            }
        }
        // create new with defaults
        ApplicationContext applicationContext = create(ApplicationContext.class, contextModel);
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


    public void initializeContext(RuntimeContext runtimeContext, ScopedContext scopedContext)
    {
        if (scopedContext.isInitialized())
        {
            return;
        }
        scopedContext.init(runtimeContext, expressionService, actionService);
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


    public void updateApplicationContext(String name, ApplicationContext applicationContext)
    {
        if (applicationContext != null && applicationContext.isDirty())
        {
            applicationService.updateApplicationContext(name, applicationContext);
            applicationContext.markClean();
        }
    }
}
