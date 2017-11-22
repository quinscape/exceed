package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.model.meta.ApplicationError;
import de.quinscape.exceed.model.startup.AppState;
import de.quinscape.exceed.model.startup.ExceedConfig;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.stream.ClassPathResourceRoot;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.ComponentRegistryImpl;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Executes the default application setup.
 * <p>
 * The webapp's /WEB-INF/extension dir is set up as only application with the context path
 * being used as application name.
 */
@Configuration
public class DefaultAppConfiguration
{
    private final static Logger log = LoggerFactory.getLogger(DefaultAppConfiguration.class);

    public static final String EXCEED_STARTUP_CONFIG = "/WEB-INF/cfg/startup.json";

    private final ServletContext servletContext;

    private final ApplicationService applicationService;

    private final ComponentRegistryImpl componentRegistry;

    private final ExceedConfig exceedConfig;

    private final Environment env;


    @Autowired
    public DefaultAppConfiguration(
        ServletContext servletContext,
        Environment env,
        ApplicationService applicationService,
        ComponentRegistryImpl componentRegistry,
        ExceedConfig exceedConfig
    )
    {
        this.servletContext = servletContext;
        this.applicationService = applicationService;
        this.componentRegistry = componentRegistry;
        this.exceedConfig = exceedConfig;
        this.env = env;
    }


    @PostConstruct
    public void initialize() throws IOException
    {
        registerComponents();
        initializeApps();
    }
    
    private void registerComponents() throws IOException
    {
        File exceedLibrarySource = Util.getExceedLibrarySource();

        ResourceRoot baseComponentRoot;
        if (exceedLibrarySource != null)
        {
            baseComponentRoot = new FileResourceRoot(
                new File(exceedLibrarySource, Util.toSlashPath("src/main/js/components")), true);
        }
        else
        {
            baseComponentRoot = new ClassPathResourceRoot("de/quinscape/exceed/components");
        }

        componentRegistry.registerComponents(baseComponentRoot);

//      XXX: disable local components for now
//        String realPath = servletContext.getRealPath("/components");
//        if (realPath != null)
//        {
//            log.info("Using file access for local components");
//            componentRegistry.registerComponents(new FileResourceRoot(new File(realPath), true));
//        }
//        else
//        {
//            log.info("Using servlet resource access for local components");
//            componentRegistry.registerComponents(new ServletResourceRoot(servletContext, "/components"));
//        }

    }


    private void initializeApps()
    {
        applicationService.startup(servletContext, exceedConfig, env);
    }


    @EventListener(ContextRefreshedEvent.class)
    public void contextRefreshedEvent()
    {

        final List<AppState> activeApplications = applicationService.getActiveApplications();

        Set<String> appWithErrors = new HashSet<>();

        for (AppState state : activeApplications)
        {
            final String appName = state.getName();
            final RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(
                appName
            );
                
            final Set<ApplicationError> errors = runtimeApplication.getApplicationModel().getMetaData().getErrors();

            final int numberOfErrors = errors.size();
            if (numberOfErrors > 0)
            {
                log.error("{} Errors in application '{}':\n{}", numberOfErrors, appName, Util.join(errors, "\n"));

                final ApplicationStatus status = state.getStatus();
                if (status == ApplicationStatus.PRODUCTION)
                {
                    //applicationService.setStatus(servletContext, appName, ApplicationStatus.OFFLINE);
                    appWithErrors.add(appName);
                }
            }
        }


        log.info("***************************************************************************");
        log.info("*");
        if (appWithErrors.size() > 0)
        {
            log.info("*  Exceed Application Container  ");
        }
        else
        {
            log.info("*  Exceed Application Container started");
        }
        log.info("*");
        log.info("*  Active applications:");
        log.info("*");
        activeApplications.forEach(appState -> {

            final RuntimeApplication app = applicationService.getRuntimeApplication(appState.getName());

            if (!appWithErrors.contains(appState.getName()))
            {
                log.info("*    Application: {}", appState.getName());
                log.info("*    Extensions: {}", appState.getExtensions());
                log.info("*    Stages: {}", app.getApplicationModel().getMetaData().getActiveStageNames());
            }
            log.info("*");
        });

        final Map<String, ExceedDataSource> sharedDataSources = applicationService.getSharedDataSources();

        if (sharedDataSources.size() > 0)
        {
            log.info("*  Shared Data Sources:");

            for (String dataSourceName : sharedDataSources.keySet())
            {
                final String usedBy = Util.join(
                    applicationService.getSharedDataSourceToApp().get(dataSourceName),
                    ", "
                );

                log.info("    Data source '{}': {}", dataSourceName, usedBy);
            }
        }

        if (appWithErrors.size() > 0)
        {
            log.error("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            log.error("X    ERRORS IN: {}", Util.join(appWithErrors, ", "));
            log.error("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        }
        else
        {
            log.info("***************************************************************************");
        }
    }
}
