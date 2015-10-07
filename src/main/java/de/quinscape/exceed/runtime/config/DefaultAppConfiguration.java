package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.runtime.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.svenson.util.Util;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;


/**
 * Executes the default application setup.
 *
 * The webapp's /WEB-INF/extension dir is set up as only application with the context path
 * being used as application name.
 *
 */
@Configuration
public class DefaultAppConfiguration
{
    private static Logger log = LoggerFactory.getLogger(DefaultAppConfiguration.class);


    private final static String ROOT_NAME = "exceed-root";

    @Autowired
    private ServletContext servletContext;
    @Autowired
    private ApplicationService applicationService;

    @PostConstruct
    public void initializeDefaultApp()
    {

        String defaultAppName = getDefaultApplicationName();

        String extensionPath = getExtensionBasePath();

        String extensions = System.getProperty("exceed.application.extensions");

        AppState applicationState = applicationService.getApplicationState(defaultAppName);
        if (applicationState == null)
        {
            log.info("Activating default application {}: path = {}, extensions = {}", defaultAppName, extensions);
            // if no application state exists, we activate this app
            applicationService.activateApplication(servletContext, defaultAppName, extensionPath, extensions);
        }
        else
        {
            log.debug("Updating  application {}: path = {}, extensions = {}", defaultAppName, extensions);
            applicationService.updateApplication(servletContext, defaultAppName, extensionPath, extensions);
        }
    }

    private String getExtensionBasePath()
    {
        String configPath = servletContext.getRealPath("/WEB-INF/cfg/exceed-app.properties");
        String extensionPath;
        boolean runningFromFileSystem = configPath != null;
        if (runningFromFileSystem)
        {
            extensionPath = "/WEB-INF/extensions";
        }
        else
        {
            extensionPath = "classpath:/WEB-INF/extensions";
        }

        log.debug("Starting default application from config {}", extensionPath);
        return extensionPath;
    }

    public String getDefaultApplicationName()
    {
        String contextPath = servletContext.getContextPath();

        if (contextPath == null)
        {
            return ROOT_NAME;
        }

        if (!contextPath.startsWith("/"))
        {
            throw new IllegalStateException("Context-path must start with /");
        }

        String defaultApplicationName = contextPath.substring(1).replace('/', '_');

        if (defaultApplicationName.equals(ROOT_NAME))
        {
            throw new IllegalStateException("Invalid context-path " + contextPath + ", the resulting application name" +
                " " + ROOT_NAME + " is reserved for the empty context path.");
        }
        return defaultApplicationName;
    }
}
