package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.model.Application;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.resource.ApplicationResources;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.stream.ClassPathResourceRoot;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.stream.ServletResourceRoot;
import de.quinscape.exceed.runtime.util.Util;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private ComponentRegistry componentRegistry;

    @Autowired
    private StyleService styleService;

    @Autowired
    private ViewDataService viewDataService;

    public RuntimeApplication createRuntimeApplication(ServletContext servletContext, AppState state)
    {
        List<ResourceRoot> resourceRoots = configureExtensions(servletContext, state);

        log.info("Creating runtime application '{}', extensions = ", state.getName(), resourceRoots);

        ApplicationResources applicationResources = resourceLoader.lookupResources(resourceRoots);
        boolean production = ApplicationStatus.from(state) == ApplicationStatus.PRODUCTION;
        Application applicationModel = modelCompositionService.compose(applicationResources);

        String collectedStyles = collectApplicationStyles(servletContext, applicationModel);

        return new RuntimeApplication(servletContext, applicationModel, collectedStyles, viewDataService);
    }

    private String collectApplicationStyles(ServletContext servletContext, Application applicationModel)
    {

        try
        {
            StringBuilder sb = new StringBuilder();

            // the styleSheets declarations in the app model are relative to the servlet context root path
            ServletResourceRoot resourceRoot = new ServletResourceRoot(servletContext, "");

            for (String name : applicationModel.getStyleSheets())
            {
                sb.append("/* APP '")
                    .append(name)
                    .append("' */\n")
                    .append(styleService.process(resourceRoot, name))
                    .append('\n');
            }

            collectComponentStyles(applicationModel, sb);

            return sb.toString();
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    private void collectComponentStyles(Application applicationModel, StringBuilder out)
    {
        Set<String> usedComponents = findUsedComponents(applicationModel);
        for (String name : usedComponents)
        {
            if (Character.isUpperCase(name.charAt(0)))
            {
                ComponentRegistration registration = componentRegistry
                    .getComponentRegistration(name);

                out.append("/* COMPONENT '")
                    .append(name)
                    .append("' */\n")
                    .append(registration.getStyles())
                    .append('\n');
            }
        }
    }

    private Set<String> findUsedComponents(Application applicationModel)
    {
        Set<String> usedComponents = new HashSet<>();
        for (View view : applicationModel.getViews().values())
        {
            addComponentsRecursive(view.getRoot(), usedComponents);
        }

        return usedComponents;
    }

    private void addComponentsRecursive(ComponentModel component, Set<String> usedComponents)
    {
        usedComponents.add(component.getName());
        for (ComponentModel kid : component.children())
        {
            addComponentsRecursive(kid, usedComponents);
        }
    }

    private List<ResourceRoot> configureExtensions(ServletContext servletContext, AppState state)
    {
        List<ResourceRoot> resourceRoots = new ArrayList<>();
        String appName = state.getName();
        String extensionPath = state.getPath();
        String extensionNames = state.getExtensions();
        resourceRoots.add(getBaseExtension(appName));

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
                    resourceRoots.add(new ClassPathResourceRoot(base + "/" + extension));
                }
                else
                {
                    resourceRoots.add(new FileResourceRoot( new File(servletContext.getRealPath(extensionPath + "/" +
                        extension))));
                }
            }
        }
        return resourceRoots;
    }

    private ResourceRoot getBaseExtension(String appName)
    {
        File sourceDir = Util.getExceedLibrarySource();
        if (sourceDir != null)
        {
            File modelSourceLocation = new File(sourceDir, "./src/main/models");

            log.info("Using model source location {} for application {}", modelSourceLocation.getPath(), appName);

            return new FileResourceRoot(modelSourceLocation);
        }
        else
        {
            String classPath = "de/quinscape/exceed/models";
            log.info("Using class path location {} for application {}", classPath, appName);
            return new ClassPathResourceRoot(classPath);
        }
    }
}

