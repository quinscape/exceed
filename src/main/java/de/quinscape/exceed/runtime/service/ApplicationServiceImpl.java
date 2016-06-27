package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.domain.tables.records.AppStateRecord;
import de.quinscape.exceed.runtime.application.ApplicationNotFoundException;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final static Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private RuntimeApplicationFactory runtimeApplicationFactory;

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
            removed.setStatus(status);
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
            RuntimeApplication runtimeApplication = holder.getRuntimeApplication(servletContext);
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
            ).fetchInto(AppState.class);
    }

    @Override
    public DefaultRuntimeApplication getRuntimeApplication(ServletContext servletContext, String appName)
    {
        ApplicationHolder applicationHolder = applications.get(appName);
        if (applicationHolder == null)
        {
            throw new ApplicationNotFoundException("Application '" + appName + "' not found");
        }

        return applicationHolder.getRuntimeApplication(servletContext);
    }


    @Override
    public void signalStyleChanges()
    {
        for (ApplicationHolder holder : applications.values())
        {
            DefaultRuntimeApplication runtimeApplication = holder.getRuntimeApplication(null);
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
            DefaultRuntimeApplication runtimeApplication = holder.getRuntimeApplication(null);
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
            DefaultRuntimeApplication runtimeApplication = holder.getRuntimeApplication(null);
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
            DefaultRuntimeApplication runtimeApplication = holder.getRuntimeApplication(null);
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

        public DefaultRuntimeApplication getRuntimeApplication(ServletContext servletContext)
        {
            if (status == ApplicationStatus.OFFLINE || servletContext == null)
            {
                return runtimeApplication;
            }

            if (runtimeApplication == null)
            {
                synchronized (this)
                {
                    if (runtimeApplication == null)
                    {
                        AppState applicationState = getApplicationState(name);
                        runtimeApplication = runtimeApplicationFactory.createRuntimeApplication(servletContext, applicationState);
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
}
