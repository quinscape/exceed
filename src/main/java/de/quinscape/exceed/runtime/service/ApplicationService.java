package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.model.startup.ExceedConfig;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.model.startup.AppState;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Set;

public interface ApplicationService
    extends ApplicationListener<ContextClosedEvent>
{
    AppState getApplicationState(String name);

    void setStatus(String appName, ApplicationStatus status);

    void updateApplication(AppState appState);

    List<AppState> getActiveApplications();

    RuntimeApplication getRuntimeApplication(String appName);

    DefaultRuntimeApplication resetRuntimeApplication(String appName);

    void signalStyleChanges();

    void signalCodeChanges();

    void signalComponentChanges(Set<String> componentNames);

    String getDefaultApplication();

    void update(ServletContext servletContext, ExceedConfig exceedConfig);
}
