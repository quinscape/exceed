package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.DBUtil;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.jooq.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;

public class ScopedContextFactory
{
    private final static Logger log = LoggerFactory.getLogger(ScopedContextFactory.class);

    private ApplicationService applicationService;

    public final static String APP_CONFIG_TYPE = "AppConfig";
    public final static String USER_CONFIG_TYPE = "AppUserConfig";

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

    public UserContext createUserContext(ContextModel contextModel, String login)
    {
        if (login == null)
        {
            throw new IllegalArgumentException("login can't be null");
        }

        UserContext userContext = create(UserContext.class, contextModel);
        userContext.setLogin(login);
        return userContext;
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
            final String appName = runtimeContext.getApplicationModel().getName();
            final DomainType appConfigType = runtimeContext.getApplicationModel().getDomainType(APP_CONFIG_TYPE);
            DomainObject domainObject = queryConfig(
                runtimeContext,
                appConfigType.getName(),

                DBUtil.jooqField(appConfigType, "appName")
                    .eq(
                        appName
                    )
            );

            if (domainObject != null)
            {
                input = domainObject.asMap();
            }
            //noinspection unchecked
        }
        else if (scopedContext instanceof UserContext)
        {
            final DomainType userConfigType = runtimeContext.getApplicationModel().getDomainType(USER_CONFIG_TYPE);
            DomainObject domainObject = queryConfig(
                runtimeContext,
                userConfigType.getName(),

                DBUtil.jooqField(userConfigType, "login")
                    .eq(
                        runtimeContext.getAuthentication().getUserName()
                    )
            );

            if (domainObject != null)
            {
                input = domainObject.asMap();
            }
        }

        scopedContext.init(runtimeContext, jsEnvironment , input);
    }


    private DomainObject queryConfig(
        RuntimeContext runtimeContext, String domainTypeName, Condition condition
    )
    {
        return DomainUtil.queryOne(
            runtimeContext,
            domainTypeName,
            condition
        );
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
        else if (cls.equals(UserContext.class))
        {
            scopedContext = new UserContext(contextModel);
        }
        else
        {
            throw new IllegalArgumentException("Cannot create scope of type " + cls);
        }
        return (T) scopedContext;
    }


    public void updateApplicationContext(RuntimeContext runtimeContext, String name, ApplicationContext applicationContext)
    {
        if (applicationContext != null && applicationContext.isDirty())
        {
             DomainObject domainObject = queryConfig(runtimeContext,
                APP_CONFIG_TYPE,
                DBUtil.jooqField(runtimeContext.getApplicationModel().getDomainType(APP_CONFIG_TYPE), "appName")
                    .eq(name)
            );

            final boolean isNew = domainObject == null;
            if (isNew)
            {
                domainObject = (GenericDomainObject) runtimeContext.getDomainService().create(runtimeContext, APP_CONFIG_TYPE, UUID.randomUUID().toString());
                domainObject.setProperty("appName", name);
            }

            domainObject.setDomainType(APP_CONFIG_TYPE);

            for (ScopedPropertyModel scopedPropertyModel : applicationContext.getContextModel().getProperties()
                .values())
            {
                final String propertyName = scopedPropertyModel.getName();
                final Object value = applicationContext.getProperty(propertyName);
                domainObject.setProperty(propertyName, value);
            }

            if (isNew)
            {
                domainObject.insert(runtimeContext);
            }
            else
            {
                domainObject.update(runtimeContext);
            }

            applicationContext.markClean();
        }
    }


    public ViewContext createViewContext(View view)
    {
        return new ViewContext(view.getContextModel(), view.getName());
    }


    public void updateUserContext(RuntimeContext runtimeContext, UserContext userContext)
    {
        final AppAuthentication authentication = runtimeContext.getAuthentication();
        if (userContext != null && userContext.isDirty() && !authentication.isAnonymous())
        {
            final String login = authentication.getUserName();
            DomainObject domainObject = queryConfig(runtimeContext,
                USER_CONFIG_TYPE,
                DBUtil.jooqField(runtimeContext.getApplicationModel().getDomainType(USER_CONFIG_TYPE), "login")
                    .eq(login)
            );

            final boolean isNew = domainObject == null;
            if (isNew)
            {
                domainObject = (GenericDomainObject) runtimeContext.getDomainService().create(runtimeContext, USER_CONFIG_TYPE, UUID.randomUUID().toString());
                domainObject.setProperty("login", login);
            }

            domainObject.setDomainType(USER_CONFIG_TYPE);

            for (ScopedPropertyModel scopedPropertyModel : userContext.getContextModel().getProperties()
                .values())
            {
                final String propertyName = scopedPropertyModel.getName();
                final Object value = userContext.getProperty(propertyName);
                domainObject.setProperty(propertyName, value);
            }
            
            if (isNew)
            {
                domainObject.insert(runtimeContext);
            }
            else
            {
                domainObject.update(runtimeContext);
            }

            userContext.markClean();
        }
    }
}
