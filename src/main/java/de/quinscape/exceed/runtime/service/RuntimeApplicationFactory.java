package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.resource.ApplicationResources;
import de.quinscape.exceed.runtime.resource.Extension;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.classpath.ClassPathExtension;
import de.quinscape.exceed.runtime.resource.file.FileBasedExtension;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Service
public class RuntimeApplicationFactory
{
    private static final String CLASSPATH_PREFIX = "classpath:";

    private static Logger log = LoggerFactory.getLogger(RuntimeApplicationFactory.class);

    @Autowired
    private ModelCompositionService modelCompositionService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RuntimeContextFactory runtimeContextFactory;

    public RuntimeApplication createRuntimeApplication(ServletContext servletContext, AppState state)
    {
        List<Extension> extensions = configureExtensions(servletContext, state);

        log.info("Creating runtime application '{}', extensions = ", state.getName(), extensions);

        ApplicationResources applicationResources = resourceLoader.lookupResources(extensions);
        boolean production = ApplicationStatus.from(state) == ApplicationStatus.PRODUCTION;
        ApplicationModel applicationModel = modelCompositionService.compose(applicationResources);

        return new RuntimeApplication(servletContext, applicationModel);
    }

    private List<Extension> configureExtensions(ServletContext servletContext, AppState state)
    {
        List<Extension> extensions = new ArrayList<>();
        String appName = state.getName();
        String extensionPath = state.getPath();
        String extensionNames = state.getExtensions();
        extensions.add(getBaseExtension(appName));

        boolean extensionsInClasspath = extensionPath.startsWith("classpath:");

        if (StringUtils.hasText(extensionNames))
        {
            StringTokenizer tokenizer = new StringTokenizer(extensionNames, ",");
            while (tokenizer.hasMoreElements())
            {
                String extension = tokenizer.nextToken().trim();

                if (extensionsInClasspath)
                {
                    String base = extensionPath.substring(CLASSPATH_PREFIX.length());
                    extensions.add(new ClassPathExtension(base + "/" + extension));
                }
                else
                {
                    extensions.add(new FileBasedExtension( new File(servletContext.getRealPath(extensionPath + "/" +
                        extension))));
                }
            }
        }
        return extensions;
    }

    private Extension getBaseExtension(String appName)
    {
        File sourceDir = Util.getExceedLibrarySource();
        if (sourceDir != null)
        {
            File modelSourceLocation = new File(sourceDir, "./src/main/resources/de/quinscape/exceed/model/base");

            log.info("Using model source location {} for application {}", modelSourceLocation.getPath(), appName);

            return new FileBasedExtension(modelSourceLocation);
        }
        else
        {
            String classPath = "de/quinscape/exceed/model/base";
            log.info("Using class path location {} for application {}", classPath, appName);
            return new ClassPathExtension(classPath);
        }
    }
}

