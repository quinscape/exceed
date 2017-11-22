package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.startup.AppState;
import de.quinscape.exceed.model.startup.ExceedConfig;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Map;
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

    void startup(ServletContext servletContext, ExceedConfig exceedConfig, Environment env);

    DomainService getDomainService(String appName);

    Model waitForChange(String appName) throws InterruptedException;

    String getCollectedStyles(String appName);

    ResourceLoader getResourceLoader(String appName);

    Map<String, ExceedDataSource> getSharedDataSources();

    Map<String, List<String>> getSharedDataSourceToApp();
}
