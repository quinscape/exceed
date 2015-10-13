package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.stream.ClassPathResourceRoot;
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
    private ComponentRegistry componentRegistry;

    @Autowired
    private StyleService styleService;

    @Autowired
    private ViewDataService viewDataService;

    public RuntimeApplication createRuntimeApplication(ServletContext servletContext, AppState state)
    {
        List<ResourceRoot> resourceRoots = configureExtensions(servletContext, state);

        log.info("Creating runtime application '{}', roots = {}", state.getName(), resourceRoots);

        return new RuntimeApplication(servletContext, viewDataService, componentRegistry, styleService,  modelCompositionService, resourceRoots);
    }

    private List<ResourceRoot> configureExtensions(ServletContext servletContext, AppState state)
    {
        try
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
                        resourceRoots.add(
                            new FileResourceRoot(
                                new File(servletContext.getRealPath(extensionPath + "/" + extension)),
                                true
                            )
                        );
                    }
                }
            }
            return resourceRoots;
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    private ResourceRoot getBaseExtension(String appName)
    {
        File sourceDir = Util.getExceedLibrarySource();
        if (sourceDir != null)
        {
            File modelSourceLocation = new File(sourceDir, "./src/main/base").getAbsoluteFile();

            log.info("Using model source location {} for application {}", modelSourceLocation.getPath(), appName);

            try
            {
                return new FileResourceRoot(modelSourceLocation, true);
            }
            catch (IOException e)
            {
                throw new ExceedRuntimeException(e);
            }
        }
        else
        {
            String classPath = "de/quinscape/exceed/base";
            log.info("Using class path location {} for application {}", classPath, appName);
            return new ClassPathResourceRoot(classPath);
        }
    }
}
