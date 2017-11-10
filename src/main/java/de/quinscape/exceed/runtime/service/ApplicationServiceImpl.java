package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.model.startup.ExceedConfig;
import de.quinscape.exceed.runtime.application.ApplicationNotFoundException;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.service.client.ClientStateService;
import de.quinscape.exceed.model.startup.AppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl
    implements ApplicationService
{
    public final static String DEFAULT_APP_PROPERTY = "exceed.default-app";

    private final static Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private final ApplicationContext applicationContext;

    private final RuntimeApplicationFactory runtimeApplicationFactory;

    private final ClientStateService clientStateService;

    private final ConcurrentMap<String, ApplicationHolder> applications = new ConcurrentHashMap<>();

    private final ServletContext servletContext;

    private volatile ExceedConfig exceedConfig;

    @Autowired
    public ApplicationServiceImpl(
        ApplicationContext applicationContext,
        RuntimeApplicationFactory runtimeApplicationFactory,
        ClientStateService clientStateService,
        ServletContext servletContext
    )
    {
        this.applicationContext = applicationContext;
        this.runtimeApplicationFactory = runtimeApplicationFactory;
        this.clientStateService = clientStateService;
        this.servletContext = servletContext;
    }


    @Override
    public AppState getApplicationState(String name)
    {
        return holder(name).getState();
    }


    private ApplicationHolder holder(String name)
    {
        final ApplicationHolder h = applications.get(name);
        if (h == null)
        {
            throw new IllegalArgumentException("Application '" + name + "' not found");
        }
        return h;
    }


    @Override
    public void setStatus(String appName, ApplicationStatus status)
    {
        final AppState state = holder(appName).getState().buildCopy()
            .withStatus(status)
            .build();
        
        updateApplication(state);
    }


    @Override
    public void updateApplication(AppState appState)
    {
        final String appName = appState.getName();

        final ApplicationStatus status = appState.getStatus();
        if (status == ApplicationStatus.OFFLINE)
        {
            log.info("Stopping application '{}'", appName);

            // TODO: Notify applications of being taken offline?
            ApplicationHolder removed = applications.remove(appName);
            if (removed != null)
            {
                removed.setState(appState);
            }
        }
        else
        {
            log.info("Starting application '{}'", appName);

            ApplicationHolder holder = new ApplicationHolder(appState);
            ApplicationHolder existing = applications.putIfAbsent(appName, holder);
            if (existing != null)
            {
                holder = existing;
            }
            holder.setState(appState);

            holder.getRuntimeApplication(servletContext, false);
        }
    }

    @Override
    public List<AppState> getActiveApplications()
    {
        return applications.values()
            .stream()
            .map(ApplicationHolder::getState)
            .collect(Collectors.toList());
    }

    @Override
    public DefaultRuntimeApplication getRuntimeApplication(String appName)
    {
        ensureReady();

        ApplicationHolder applicationHolder = applications.get(appName);
        if (applicationHolder == null)
        {
            throw new ApplicationNotFoundException("Application '" + appName + "' not found");
        }

        return applicationHolder.getRuntimeApplication(servletContext, false);
    }


    @Override
    public DefaultRuntimeApplication resetRuntimeApplication(String appName)
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

        private volatile AppState appState;
        private volatile DefaultRuntimeApplication runtimeApplication;

        private AppState state;


        public ApplicationHolder(AppState appState)
        {
            this.name = appState.getName();
            this.appState = appState;
        }

        public DefaultRuntimeApplication getRuntimeApplication(ServletContext servletContext, boolean forceRefresh)
        {
            if (appState.getStatus() == ApplicationStatus.OFFLINE || servletContext == null)
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

        public AppState getState()
        {
            return state;
        }


        public void setState(AppState state)
        {
            this.state = state;
        }
    }


    public synchronized String getDefaultApplication()
    {
        ensureReady();
        return exceedConfig.getDefaultApp();
    }


    private void ensureReady()
    {
        if (exceedConfig == null)
        {
            throw new ServiceNotReadyException("Application service not updated with exceed config yet");
        }
    }


    @Override
    public void update(ServletContext servletContext, ExceedConfig exceedConfig)
    {
        for (AppState appState : exceedConfig.getApps())
        {
            final String appName = appState.getName();

            log.info("Initializing exceed application '{}' ( extensions: {} )", appName, appState.getExtensions());

            updateApplication(appState);
        }
        
        this.exceedConfig = exceedConfig;
    }

}
