package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Set;

public interface ApplicationService
    extends ApplicationListener<ContextClosedEvent>
{
    AppState getApplicationState(String name);

    @Transactional(propagation = Propagation.NESTED)
    void activateApplication(ServletContext servletContext, String name, String path, String extensions);

    @Transactional(propagation = Propagation.NESTED)
    int dropApplication(String name);

    @Transactional(propagation = Propagation.NESTED)
    void setStatus(ServletContext servletContext, String appName, ApplicationStatus status);

    @Transactional(propagation = Propagation.NESTED)
    void updateApplication(ServletContext servletContext, String appName, String path, String extensions);

    @Transactional(propagation = Propagation.NESTED)
    List<AppState> getActiveApplications();

    RuntimeApplication getRuntimeApplication(ServletContext servletContext, String appName);

    void signalStyleChanges();

    void signalCodeChanges();

    void signalComponentChanges(Set<String> componentNames);
}
