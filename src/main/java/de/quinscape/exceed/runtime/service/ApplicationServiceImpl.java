package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.domain.tables.records.AppStateRecord;
import de.quinscape.exceed.runtime.application.ApplicationNotFoundException;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.service.client.ClientStateService;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.quinscape.exceed.domain.Tables.*;

@Service
@Transactional
public class ApplicationServiceImpl
    implements ApplicationService
{
    public final static String DEFAULT_APP_PROPERTY = "exceed.default-app";

    private final static Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private ApplicationContext applicationContext;

    private DSLContext dslContext;

    private RuntimeApplicationFactory runtimeApplicationFactory;

    private ClientStateService clientStateService;


    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }


    @Autowired
    public void setDslContext(DSLContext dslContext)
    {
        this.dslContext = dslContext;
    }


    @Autowired
    public void setRuntimeApplicationFactory(RuntimeApplicationFactory runtimeApplicationFactory)
    {
        this.runtimeApplicationFactory = runtimeApplicationFactory;
    }


    @Autowired
    public void setClientStateService(ClientStateService clientStateService)
    {
        this.clientStateService = clientStateService;
    }


    private ConcurrentMap<String, ApplicationHolder> applications = new ConcurrentHashMap<>();


    @Override
    public AppState getApplicationState(String name)
    {
        return dslContext.select().from(APP_STATE).where(APP_STATE.NAME.eq(name)).fetchOneInto(AppState.class);
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void activateApplication(ServletContext servletContext, String name, String path, String extensions)
    {
        if (servletContext == null)
        {
            throw new IllegalArgumentException("servletContext can't be null");
        }

        AppState appState = new AppState();
        appState.setId(UUID.randomUUID().toString());
        appState.setName(name);
        appState.setPath(path);
        appState.setStatus(ApplicationStatus.PRODUCTION.ordinal());
        appState.setExtensions(extensions);

        AppStateRecord record = dslContext.newRecord(APP_STATE, appState);
        record.store();

        updateApplicationsInternal(servletContext, name, ApplicationStatus.PRODUCTION);
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public int dropApplication(String name)
    {
        return dslContext.deleteFrom(APP_STATE).where(APP_STATE.NAME.eq(name)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void setStatus(ServletContext servletContext, String appName, ApplicationStatus status)
    {
        if (servletContext == null)
        {
            throw new IllegalArgumentException("servletContext can't be null");
        }

        AppState applicationState = getApplicationState(appName);

        if (applicationState == null)
        {
            throw new IllegalArgumentException("Application '" + appName + "' not found.");
        }

        ApplicationStatus current =  ApplicationStatus.from(applicationState);
        if (!current.hasValidTransitionTo(status))
        {
            throw new IllegalStateException("Error updating '" + appName + "': Cannot go from " + current + " to " + status);
        }

        dslContext.update(APP_STATE)
            .set(APP_STATE.STATUS, status.ordinal())
            .where(APP_STATE.NAME.eq(appName))
            .execute();

        updateApplicationsInternal(servletContext, appName, status);
    }

    private void updateApplicationsInternal(ServletContext servletContext, String appName, ApplicationStatus status)
    {
        if (status == ApplicationStatus.OFFLINE)
        {
            log.info("Stopping application '{}'", appName);

            // TODO: Notify applications of being taken offline?
            ApplicationHolder removed = applications.remove(appName);
            if (removed != null)
            {
                removed.setStatus(status);
            }
        }
        else
        {
            log.info("Starting application '{}'", appName);

            ApplicationHolder holder = new ApplicationHolder(appName);
            ApplicationHolder existing = applications.putIfAbsent(appName, holder);
            if (existing != null)
            {
                holder = existing;
            }

            holder.setStatus(status);
            RuntimeApplication runtimeApplication = holder.getRuntimeApplication(servletContext, false);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void updateApplication(ServletContext servletContext, String appName, String path, String extensions)
    {
        AppState applicationState = getApplicationState(appName);
        if (applicationState == null)
        {
            throw new IllegalArgumentException("Application '" + appName + "' not found.");
        }

        applicationState.setPath(path);
        applicationState.setExtensions(extensions);

        AppStateRecord record = dslContext.newRecord(APP_STATE, applicationState);
        dslContext.executeUpdate(record);

        updateApplicationsInternal(servletContext, appName, ApplicationStatus.from(applicationState));
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public List<AppState> getActiveApplications()
    {
        return dslContext.select().from(APP_STATE)
            .where(
                APP_STATE.STATUS.in(
                    ApplicationStatus.PREVIEW.ordinal(),
                    ApplicationStatus.PRODUCTION.ordinal()
                )
            )
            .orderBy(APP_STATE.NAME)
            .fetchInto(AppState.class);
    }

    @Override
    public DefaultRuntimeApplication getRuntimeApplication(ServletContext servletContext, String appName)
    {
        ApplicationHolder applicationHolder = applications.get(appName);
        if (applicationHolder == null)
        {
            throw new ApplicationNotFoundException("Application '" + appName + "' not found");
        }

        return applicationHolder.getRuntimeApplication(servletContext, false);
    }


    @Override
    public DefaultRuntimeApplication resetRuntimeApplication(ServletContext servletContext, String appName)
    {
        ApplicationHolder applicationHolder = applications.get(appName);
        if (applicationHolder == null)
        {
            throw new ApplicationNotFoundException("Application '" + appName + "' not found");
        }

        return applicationHolder.getRuntimeApplication(servletContext, true);
    }


    @Override
    public void signalStyleChanges()
    {
        for (ApplicationHolder holder : applications.values())
        {
            DefaultRuntimeApplication runtimeApplication = holder.getRuntimeApplication(null, false);
            if (runtimeApplication != null)
            {
                runtimeApplication.notifyStyleChange();
            }
        }
    }


    @Override
    public void signalCodeChanges()
    {
        for (ApplicationHolder holder : applications.values())
        {
            DefaultRuntimeApplication runtimeApplication = holder.getRuntimeApplication(null, false);
            if (runtimeApplication != null)
            {
                runtimeApplication.notifyCodeChange();
            }
        }
    }


    @Override
    public void signalComponentChanges(Set<String> componentNames)
    {
        for (ApplicationHolder holder : applications.values())
        {
            DefaultRuntimeApplication runtimeApplication = holder.getRuntimeApplication(null, false);
            if (runtimeApplication != null)
            {
                runtimeApplication.signalComponentChanges(componentNames);
            }
        }

    }


    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void updateApplicationContext(String appName, String json)
    {
        log.info("Updating scoped context for {}", json);

        dslContext.update(APP_STATE)
            .set(APP_STATE.CONTEXT, json)
            .where(APP_STATE.NAME.eq(appName))
            .execute();
    }


    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent)
    {
        for (ApplicationHolder holder : applications.values())
        {
            DefaultRuntimeApplication runtimeApplication = holder.getRuntimeApplication(null, false);
            if (runtimeApplication != null)
            {
                runtimeApplication.notifyShutdown();
            }
        }
    }


    private class ApplicationHolder
    {
        private final String name;
        private volatile DefaultRuntimeApplication runtimeApplication;
        private volatile ApplicationStatus status;

        public ApplicationHolder(String name)
        {
            this.name = name;
        }

        public DefaultRuntimeApplication getRuntimeApplication(ServletContext servletContext, boolean forceRefresh)
        {
            if (status == ApplicationStatus.OFFLINE || servletContext == null)
            {
                return runtimeApplication;
            }

            if (forceRefresh || runtimeApplication == null)
            {
                synchronized (this)
                {
                    if (forceRefresh || runtimeApplication == null)
                    {
                        AppState applicationState = getApplicationState(name);
                        runtimeApplication = runtimeApplicationFactory.createRuntimeApplication(servletContext, applicationState);

                        if (forceRefresh)
                        {
                            clientStateService.flushApplicationScope(name);
                        }
                    }
                }
            }

            return runtimeApplication;
        }

        public void setStatus(ApplicationStatus status)
        {
            this.status = status;
        }
    }


    private volatile String defaultApp;

    public String getDefaultApplication()
    {
        if (defaultApp == null)
        {
            synchronized (this)
            {
                if (defaultApp == null)
                {
                    final String defaultAppFromProperty = applicationContext.getEnvironment().getProperty
                        (DEFAULT_APP_PROPERTY);

                    if (defaultAppFromProperty != null)
                    {
                        defaultApp = defaultAppFromProperty;
                        log.info("Using default application '{}' from enviroment property {}'", defaultApp,
                            DEFAULT_APP_PROPERTY);
                    }
                    else
                    {
                        final List<AppState> activeApplications = getActiveApplications();
                        if (activeApplications.size() == 0)
                        {
                            throw new IllegalStateException("No active applications");
                        }
                        defaultApp = activeApplications.get(0).getName();
                        log.info("Using first active application '{}' as default application", defaultApp);
                    }
                }
            }
        }
        return defaultApp;
    }

}
