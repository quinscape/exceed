package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.stream.ClassPathResourceRoot;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.stream.ServletResourceRoot;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.ComponentRegistryImpl;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;


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
    private final static Logger log = LoggerFactory.getLogger(DefaultAppConfiguration.class);


    private final static String ROOT_NAME = "exceed-root";

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ComponentRegistryImpl componentRegistry;

    @Autowired
    private Environment env;

    @PostConstruct
    public void initialize() throws IOException
    {
        registerComponents();
        initializeDefaultApp();
    }

    private void registerComponents() throws IOException
    {
        File exceedLibrarySource = Util.getExceedLibrarySource();

        ResourceRoot baseComponentRoot;
        if (exceedLibrarySource != null)
        {
            baseComponentRoot = new FileResourceRoot(new File(exceedLibrarySource, Util.path("src/main/js/components")), true);
        }
        else
        {
            baseComponentRoot = new ClassPathResourceRoot("de/quinscape/exceed/components");
        }

        componentRegistry.registerComponents(baseComponentRoot);

        String realPath = servletContext.getRealPath("/components");
        if (realPath != null)
        {
            log.info("Using file access for local components");
            componentRegistry.registerComponents(new FileResourceRoot(new File(realPath), true));
        }
        else
        {
            log.info("Using servlet resource access for local components");
            componentRegistry.registerComponents(new ServletResourceRoot(servletContext, "/components"));
        }

    }

    private void initializeDefaultApp()
    {
        String defaultAppName = getDefaultApplicationName();

        String extensionPath = getExtensionBasePath();

        String extensions = env.getProperty("exceed.application.extensions");

        AppState applicationState = applicationService.getApplicationState(defaultAppName);

        log.info("Initializing exceed application '{}' ( extensions: {} )", defaultAppName, extensions);

        if (applicationState == null)
        {
            log.debug("Activating default application");
            // if no application state exists, we activate this app
            applicationService.activateApplication(servletContext, defaultAppName, extensionPath, extensions);
        }
        else
        {
            log.debug("Updating default application");
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
