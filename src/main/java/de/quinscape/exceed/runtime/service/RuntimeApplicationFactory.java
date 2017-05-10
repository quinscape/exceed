package de.quinscape.exceed.runtime.service;

import com.google.common.cache.LoadingCache;
import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.action.ClientActionRenderer;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.resource.ResourceCacheFactory;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.stream.ClassPathResourceRoot;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import de.quinscape.exceed.runtime.scope.ScopedContextFactory;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateService;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.model.ModelSchemaService;
import de.quinscape.exceed.runtime.util.Util;
import de.quinscape.exceed.runtime.view.ViewDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

@Service
public class RuntimeApplicationFactory
{
    private static final String CLASSPATH_PREFIX = "classpath:";

    private final static Logger log = LoggerFactory.getLogger(RuntimeApplicationFactory.class);

    private final ApplicationContext applicationContext;

    private final ModelCompositionService modelCompositionService;

    private final ComponentRegistry componentRegistry;

    private final StyleService styleService;

    private final ViewDataService viewDataService;

    private final ResourceCacheFactory resourceCacheFactory;

    private final DomainServiceFactory domainServiceFactory;

    private final ProcessService processService;

    private final RuntimeContextFactory runtimeContextFactory;

    private final ScopedContextFactory scopedContextFactory;

    private final DomainServiceRepository domainServiceRepository;

    private final StorageConfigurationRepository storageConfigurationRepository;

    private final ClientStateService clientStateService;

    private final ModelSchemaService modelSchemaService;

    private Set<ClientStateProvider> clientStateProviders;


    @Autowired
    public RuntimeApplicationFactory(ResourceCacheFactory resourceCacheFactory, ApplicationContext applicationContext, ModelCompositionService modelCompositionService, StorageConfigurationRepository storageConfigurationRepository, ComponentRegistry componentRegistry, ClientStateService clientStateService, StyleService styleService, ViewDataService viewDataService, DomainServiceFactory domainServiceFactory, ProcessService processService, RuntimeContextFactory runtimeContextFactory, ScopedContextFactory scopedContextFactory, ModelSchemaService modelSchemaService, DomainServiceRepository domainServiceRepository)
    {
        this.resourceCacheFactory = resourceCacheFactory;
        this.applicationContext = applicationContext;
        this.modelCompositionService = modelCompositionService;
        this.storageConfigurationRepository = storageConfigurationRepository;
        this.componentRegistry = componentRegistry;
        this.clientStateService = clientStateService;
        this.styleService = styleService;
        this.viewDataService = viewDataService;
        this.domainServiceFactory = domainServiceFactory;
        this.processService = processService;
        this.runtimeContextFactory = runtimeContextFactory;
        this.scopedContextFactory = scopedContextFactory;
        this.modelSchemaService = modelSchemaService;
        this.domainServiceRepository = domainServiceRepository;
    }


    public DefaultRuntimeApplication createRuntimeApplication(ServletContext servletContext, AppState state)
    {
        List<ResourceRoot> resourceRoots = configureExtensions(servletContext, state);

        final String appName = state.getName();
        log.info("Creating runtime application '{}', roots = {}", appName, resourceRoots);

        ResourceLoader resourceLoader = new ResourceLoader(resourceRoots);
        LoadingCache<String, CachedResource> cache = resourceCacheFactory.createCache(resourceLoader);
        if (cache != null)
        {
            resourceLoader.setResourceCache(cache);
        }

        Map<String, ClientActionRenderer> generators = new HashMap<>();
        generators.put("syslog", new SyslogCallGenerator());

        final DomainService domainService = domainServiceFactory.create(modelSchemaService.getModelDomainTypes());

        domainServiceRepository.register(appName, domainService);

        return new DefaultRuntimeApplication( servletContext, viewDataService, componentRegistry, styleService,  modelCompositionService, resourceLoader, domainService, clientStateService, processService, appName, runtimeContextFactory, scopedContextFactory, storageConfigurationRepository, clientStateProviders);
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
            File modelSourceLocation = new File(sourceDir, Util.path("src/main/base")).getAbsoluteFile();

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

    @PostConstruct
    public void initProviders()
    {
        clientStateProviders = ClientStateService.findProviderBeans(applicationContext, ExceedAppProvider.class);
    }

}

