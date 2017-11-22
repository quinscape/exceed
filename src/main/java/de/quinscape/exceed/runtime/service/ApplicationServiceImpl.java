package de.quinscape.exceed.runtime.service;

import com.google.common.cache.LoadingCache;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.annotation.ResourceInjectorPredicate;
import de.quinscape.exceed.model.change.CodeChange;
import de.quinscape.exceed.model.change.Shutdown;
import de.quinscape.exceed.model.change.StyleChange;
import de.quinscape.exceed.model.change.Timeout;
import de.quinscape.exceed.model.meta.ApplicationMetaData;
import de.quinscape.exceed.model.staging.DataSourceModel;
import de.quinscape.exceed.model.staging.StageModel;
import de.quinscape.exceed.model.startup.AppState;
import de.quinscape.exceed.model.startup.ExceedConfig;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedProperties;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.application.ApplicationStatus;
import de.quinscape.exceed.runtime.application.DefaultRuntimeApplication;
import de.quinscape.exceed.runtime.application.ResourceInjector;
import de.quinscape.exceed.runtime.component.ComponentRegistration;
import de.quinscape.exceed.runtime.datasrc.DataSourceUtil;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.model.ModelMerger;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.DefaultResourceLoader;
import de.quinscape.exceed.runtime.resource.ResourceCacheFactory;
import de.quinscape.exceed.runtime.resource.ResourceChangeListener;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.resource.stream.ClassPathResourceRoot;
import de.quinscape.exceed.runtime.service.client.ClientStateService;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.FileExtension;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.Util;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.svenson.JSON;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl
    implements ApplicationService
{
    public final static String DEFAULT_APP_PROPERTY = "exceed.default-app";

    private final static long TIMEOUT = TimeUnit.SECONDS.toMillis(110);

    private static final String CLASSPATH_PREFIX = "classpath:";

    private final static Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private final ApplicationContext applicationContext;

    private final RuntimeApplicationFactory runtimeApplicationFactory;

    private final ClientStateService clientStateService;

    private final ConcurrentMap<String, ApplicationHolder> applications = new ConcurrentHashMap<>();

    private Map<String, DataSourceModel> sharedDataSourceModels;

    private Map<String, ExceedDataSource> sharedDataSources;

    private Map<String, ExceedDataSource> sharedDataSourcesRO;

    private final ServletContext servletContext;

    private final ResourceCacheFactory resourceCacheFactory;

    private final DomainServiceFactory domainServiceFactory;

    private final ModelCompositionService modelCompositionService;

    private final Definitions systemDefinitions;

    private volatile ExceedConfig exceedConfig;

    private final ResourceInjector resourceInjector;

    private final NashornScriptEngine nashorn;

    private final StyleService styleService;

    private final ComponentRegistry componentRegistry;

    private final Environment env;

    private Map<String, List<String>> sharedDataSourceToApp;


    @Autowired
    public ApplicationServiceImpl(
        ApplicationContext applicationContext,
        @Lazy
        RuntimeApplicationFactory runtimeApplicationFactory,
        ClientStateService clientStateService,
        ServletContext servletContext,
        ResourceCacheFactory resourceCacheFactory,
        DomainServiceFactory domainServiceFactory,
        ModelCompositionService modelCompositionService,
        Definitions systemDefinitions,
        Map<String, ResourceInjectorPredicate> predicates,
        NashornScriptEngine nashorn,
        StyleService styleService,
        ComponentRegistry componentRegistry,
        Environment env
    )
    {
        this.applicationContext = applicationContext;
        this.runtimeApplicationFactory = runtimeApplicationFactory;
        this.clientStateService = clientStateService;
        this.servletContext = servletContext;
        this.resourceCacheFactory = resourceCacheFactory;
        this.domainServiceFactory = domainServiceFactory;
        this.modelCompositionService = modelCompositionService;
        this.systemDefinitions = systemDefinitions;
        resourceInjector = new ResourceInjector(ApplicationMetaData.class, predicates);
        this.nashorn = nashorn;
        this.styleService = styleService;
        this.componentRegistry = componentRegistry;
        this.env = env;
    }


    @Override
    public AppState getApplicationState(String name)
    {
        return holder(name).getState();
    }


    private ApplicationHolder holder(String name)
    {
        final ApplicationHolder h = applications.get(name);
        if (h == null)
        {
            throw new IllegalArgumentException("Application '" + name + "' not found");
        }
        return h;
    }


    @Override
    public void setStatus(String appName, ApplicationStatus status)
    {
        final AppState state = holder(appName).getState().buildCopy()
            .withStatus(status)
            .build();
        
        updateApplication(state);
    }


    @Override
    public void updateApplication(AppState appState)
    {
        final String appName = appState.getName();

        final ApplicationStatus status = appState.getStatus();
        holder(appName).setState(appState);
    }

    @Override
    public List<AppState> getActiveApplications()
    {
        return applications.values()
            .stream()
            .map(ApplicationHolder::getState)
            .collect(Collectors.toList());
    }

    @Override
    public DefaultRuntimeApplication getRuntimeApplication(String appName)
    {
        ensureReady();

        ApplicationHolder holder = holder(appName);
        if (holder.getState().getStatus() == ApplicationStatus.OFFLINE)
        {
            return null;
        }
        return holder.getRuntimeApplication(servletContext, false);
    }


    @Override
    public DefaultRuntimeApplication resetRuntimeApplication(String appName)
    {
        ApplicationHolder applicationHolder = holder(appName);

        return applicationHolder.getRuntimeApplication(servletContext, true);
    }


    @Override
    public void signalStyleChanges()
    {
        for (ApplicationHolder holder : applications.values())
        {
            holder.notifyStyleChange();
        }
    }


    @Override
    public void signalCodeChanges()
    {
        for (ApplicationHolder holder : applications.values())
        {
            holder.notifyCodeChange();
        }
    }


    @Override
    public void signalComponentChanges(Set<String> componentNames)
    {
        for (ApplicationHolder holder : applications.values())
        {
            holder.signalComponentChanges(componentNames);
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent)
    {
        for (ApplicationHolder holder : applications.values())
        {
            holder.notifyShutdown();
        }
    }




    public synchronized String getDefaultApplication()
    {
        ensureReady();
        return exceedConfig.getDefaultApp();
    }


    private void ensureReady()
    {
        if (exceedConfig == null)
        {
            throw new ServiceNotReadyException("Application service not updated with exceed config yet");
        }
    }


    @Override
    public synchronized void startup(
        ServletContext servletContext, ExceedConfig exceedConfig, Environment env
    )
    {

        this.exceedConfig = exceedConfig;

        Map<String, DataSourceModel> sharedModels = new HashMap<>();

        final List<AppState> configuredApps = exceedConfig.getApps();
        List<AppArtifacts> preparedApps = new ArrayList<>(configuredApps.size());

        Map<String, List<String>> dataSrcToApp = new HashMap<>();

        for (AppState appState : configuredApps)
        {
            final AppArtifacts preparedApp = prepareApplication(
                env,
                servletContext,
                appState,
                sharedModels,
                dataSrcToApp
            );
            preparedApps.add(preparedApp);
        }

        sharedDataSourceToApp = Collections.unmodifiableMap(dataSrcToApp);
        sharedDataSourceModels = Collections.unmodifiableMap(sharedModels);

        sharedDataSources = new HashMap<>();

        DataSourceUtil.createDataSources(
            applicationContext,
            sharedDataSourceModels,
            sharedDataSources,
            "xcd-shared-",
            true
        );
        sharedDataSourcesRO = Collections.unmodifiableMap(sharedDataSources);

        for (AppArtifacts preparedApp : preparedApps)
        {
            final String appName = preparedApp.applicationModel.getName();

            log.info("Starting application '{}'", appName);

            ApplicationHolder holder = new ApplicationHolder(preparedApp);
            ApplicationHolder existing = applications.put(appName, holder);
            if (existing != null)
            {
                throw new IllegalStateException("There shouldn't be any existing holders");
            }
            holder.getRuntimeApplication(servletContext, false);
        }
    }

    public Map<String, ExceedDataSource> getSharedDataSources()
    {
        return sharedDataSourcesRO;
    }


    @Override
    public DomainService getDomainService(String appName)
    {
        return holder(appName).artifacts.domainService;
    }


    @Override
    public Model waitForChange(String appName) throws InterruptedException
    {
        return holder(appName).waitForChange(TIMEOUT);

    }


    @Override
    public String getCollectedStyles(String appName)
    {
        return holder(appName).getCollectedStyles();
    }


    @Override
    public ResourceLoader getResourceLoader(String appName)
    {
        return holder(appName).artifacts.resourceLoader;
    }




    private class ApplicationHolder
        implements ResourceChangeListener
    {
        public final AppArtifacts artifacts;

        private volatile DefaultRuntimeApplication runtimeApplication;
        private volatile Model changeModel = null;
        private volatile long lastChange;

        private volatile AppState state;

        public ApplicationHolder(AppArtifacts artifacts)
        {
            this.artifacts = artifacts;
            this.state = artifacts.initialAppState;
        }

        public DefaultRuntimeApplication getRuntimeApplication(ServletContext servletContext, boolean forceRefresh)
        {
            final String appName = artifacts.applicationModel.getName();
            final AppState appState = state;

            if (appState.getStatus() == ApplicationStatus.OFFLINE || servletContext == null)
            {
                return runtimeApplication;
            }

            if (forceRefresh || runtimeApplication == null)
            {
                synchronized (this)
                {
                    if (forceRefresh || runtimeApplication == null)
                    {
                        runtimeApplication = createApplication();

                        if (forceRefresh)
                        {
                            clientStateService.flushApplicationScope(appName);
                        }
                    }
                }
            }

            return runtimeApplication;
        }


        private DefaultRuntimeApplication createApplication()
        {
            final StageModel mergedStateModel = artifacts.mergedStateModel;
            final ApplicationModel applicationModel = artifacts.applicationModel;
            final DefaultResourceLoader resourceLoader = artifacts.resourceLoader;
            final DomainService domainService = artifacts.domainService;

            final Map<String, DataSourceModel> dataSourceModels = mergedStateModel.getDataSourceModels();

            final Map<String, ExceedDataSource> dataSources = createApplicationDataSources(
                applicationModel,
                dataSourceModels
            );

            for (ResourceRoot root : resourceLoader.getExtensions())
            {
                ResourceWatcher resourceWatcher = root.getResourceWatcher();
                if (resourceWatcher != null)
                {
                    log.debug("Register lister for watcher of resource root {}", root);
                    resourceWatcher.register(this);
                }
                else
                {
                    log.debug("Non-reloadable resource root {}", root);
                }
            }

            runtimeApplication = runtimeApplicationFactory.createRuntimeApplication(
                servletContext,
                artifacts.resourceLoader,
                domainService,
                applicationModel,
                dataSources
            );

            return runtimeApplication;
        }


        private Map<String, ExceedDataSource> createApplicationDataSources(
            ApplicationModel applicationModel, Map<String, DataSourceModel> dataSourceModels
        )
        {
            // create non-shared data sources
            final Map<String, ExceedDataSource> dataSources = new HashMap<>();


            // merge in the shared data sources
            for (DataSourceModel dataSourceModel : dataSourceModels.values())
            {
                if (dataSourceModel.isShared())
                {
                    final String name = dataSourceModel.getName();
                    final ExceedDataSource sharedDataSource = sharedDataSources.get(name);
                    dataSources.put(name, sharedDataSource);
                }
            }

            DataSourceUtil.createDataSources(
                applicationContext,
                dataSourceModels,
                dataSources,
                "xcd-" + applicationModel.getName(),
                false
            );

            return Collections.unmodifiableMap(dataSources);
        }


        public AppState getState()
        {
            return state;
        }


        public void setState(AppState state)
        {
            this.state = state;
        }


        @Override
        public synchronized void onResourceChange(
            ModuleResourceEvent resourceEvent,
            FileResourceRoot root,
            String path
        )
        {
            log.debug("onResourceChange:  {} {} ( ROOT {} ) ", resourceEvent, path, root);

            PathResources pathResources = artifacts.resourceLoader.getResources(path);

            if (pathResources == null)
            {
                return;
            }

            if (path.endsWith(FileExtension.JSON))
            {
                // for models we need to always update on any change to the path resources because merging means that
                // lower priority models can still influence the final outcome

                TopLevelModel model = modelCompositionService.update(artifacts.applicationModel, artifacts.domainService, artifacts.resourceLoader, root, pathResources);
                if (model != null)
                {
                    notifyChange(model);

                    if (model instanceof View)
                    {
                        clientStateService.flushViewScope((View) model);
                    }
                    clientStateService.flushModelVersionScope(artifacts.applicationModel.getName());
                }
                resourceInjector.updateResource(artifacts.applicationModel, nashorn, artifacts.resourceLoader, artifacts.applicationModel.getMetaData(), path);
            }
            else
            {
                // for resources, we only care for changes in the top resource.

                AppResource topResource = pathResources.getHighestPriorityResource();
                ResourceRoot rootOfTopResource = topResource.getResourceRoot();
                if (root.equals(rootOfTopResource))
                {
                    if (path.endsWith(FileExtension.CSS))
                    {
                        if (artifacts.applicationModel.getConfigModel().getStyleSheets().contains(path))
                        {
                            try
                            {
                                styleService.reload(root, path);
                            }
                            catch (IOException e)
                            {
                                throw new ExceedRuntimeException(e);
                            }
                            notifyStyleChange();
                        }
                    }
                    else if (path.equals("/resources/js/main.js"))
                    {
                        log.debug("Reload js: {}", path);
                        notifyCodeChange();
                    }
                    else
                    {
                        resourceInjector.updateResource(artifacts.applicationModel, nashorn, artifacts.resourceLoader, artifacts.applicationModel.getMetaData(), path);
                    }
                }

            }
        }

        public void notifyStyleChange()
        {
            log.debug("notifyStyleChange");

            notifyChange(StyleChange.INSTANCE);
        }


        public void notifyCodeChange()
        {
            notifyChange(CodeChange.INSTANCE);
        }


        private synchronized void notifyChange(Model changeModel)
        {
            log.debug("notifyChange", changeModel);

            this.lastChange = System.currentTimeMillis();
            this.changeModel = changeModel;
            this.notifyAll();
        }


        public synchronized Model waitForChange(long timeout) throws InterruptedException
        {
            long start = lastChange;

            while (lastChange == start)
            {
                this.wait(timeout);
                if (lastChange == start)
                {
                    return Timeout.INSTANCE;
                }
            }
            return changeModel;
        }

        public void notifyShutdown()
        {
            notifyChange(Shutdown.INSTANCE);
        }

        public String getCollectedStyles()
        {
            final ApplicationModel applicationModel = artifacts.applicationModel;
            final DefaultResourceLoader resourceLoader = artifacts.resourceLoader;

            try
            {
                StringBuilder sb = new StringBuilder();


                for (String name : applicationModel.getConfigModel().getStyleSheets())
                {
                    PathResources resourceLocation = resourceLoader.getResources(name);

                    if (resourceLocation == null)
                    {
                        log.info("RESOURCE LOCATIONS:\n{}", JSON.formatJSON(JSONUtil.DEFAULT_GENERATOR.forValue(resourceLoader
                            .getAllResources().keySet())));
                        throw new IllegalStateException("Resource '" + name + "' does not exist in any extension");
                    }

                    ResourceRoot root = resourceLocation.getHighestPriorityResource().getResourceRoot();
                    sb.append("/* APP '")
                        .append(name)
                        .append("' */\n")
                        .append(styleService.process(root, name))
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
        private void collectComponentStyles(ApplicationModel applicationModel, StringBuilder out)
        {
            Set<String> usedComponents = findUsedComponents(applicationModel);
            for (String name : usedComponents)
            {
                if (Character.isUpperCase(name.charAt(0)))
                {
                    ComponentRegistration registration = componentRegistry
                        .getComponentRegistration(name);

                    if (registration == null)
                    {
                        throw new IllegalStateException("No component registration with name '" + name + "' found");
                    }


                    String styles = registration.getStyles();
                    if (styles != null)
                    {
                        out.append("/* COMPONENT '")
                            .append(name)
                            .append("' */\n")
                            .append(styles)
                            .append('\n');
                    }
                }
            }
        }

        private Set<String> findUsedComponents(ApplicationModel applicationModel)
        {
            Set<String> usedComponents = new HashSet<>();
            for (View view : applicationModel.getViews().values())
            {
                for (ComponentModel componentModel : view.getContent().values())
                {
                    addComponentsRecursive(componentModel, usedComponents);
                }
            }

            return usedComponents;
        }


        private void addComponentsRecursive(ComponentModel component, Set<String> usedComponents)
        {
            final String componentName = component.getName();
            if (!usedComponents.contains(componentName))
            {
                usedComponents.add(componentName);
            }

            for (ComponentModel kid : component.children())
            {
                addComponentsRecursive(kid, usedComponents);
            }
        }




        public void signalComponentChanges(Set<String> componentNames)
        {
            final ApplicationModel applicationModel = artifacts.applicationModel;
            for (View view : applicationModel.getViews().values())
            {
                ComponentUtil.updateComponentRegsAndParents(componentRegistry, view, componentNames);
            }
        }

    }


    /**
     * Creates the basic resources needed to create the runtime application and determines the shared data source
     * models.
     *
     *
     * @param env
     * @param servletContext            servlet context
     * @param state                     application state model
     * @param sharedDataSourceModels    map of shared data source models, merged incrementally
     * @return app artifacts
     */
    private AppArtifacts prepareApplication(
        Environment env,
        ServletContext servletContext,
        AppState state,
        Map<String, DataSourceModel> sharedDataSourceModels,
        Map<String, List<String>> sharedDataSourceToApp
    )
    {
        final List<ResourceRoot> resourceRoots = configureExtensions(env, servletContext, state);

        final String appName = state.getName();

        log.info("Creating runtime application '{}', roots = {}", appName, resourceRoots);

        final DefaultResourceLoader resourceLoader = new DefaultResourceLoader(resourceRoots);
        LoadingCache<String, CachedResource> cache = resourceCacheFactory.createCache(resourceLoader);
        resourceLoader.setResourceCache(cache);

        final DomainService domainService = domainServiceFactory.create();

        final ApplicationModel applicationModel = modelCompositionService.compose(
            resourceLoader, domainService, systemDefinitions, appName);

        final ApplicationMetaData metaData = applicationModel.getMetaData();
        modelCompositionService.postprocess(applicationModel);

        resourceInjector.injectResources(applicationModel, nashorn, resourceLoader, metaData);

        final List<String> activeStageNames = getActiveStageNames(applicationModel, state);
        final StageModel mergedStageModel = mergeStages(this.env, applicationModel, state, activeStageNames);

        applicationModel.getMetaData().setMergedStageModel(mergedStageModel);
        applicationModel.getMetaData().setActiveStageNames(activeStageNames);

        if (log.isDebugEnabled())
        {
            log.debug("Merged stage: {}", JSONUtil.formatJSON(JSONUtil.DEFAULT_GENERATOR.forValue(mergedStageModel)));
        }


        final Map<String, ? extends DataSourceModel> dataSourceModels = mergedStageModel.getDataSourceModels();

        for (Map.Entry<String, ? extends DataSourceModel> e : dataSourceModels.entrySet())
        {
            final String dataSourceName = e.getKey();
            final DataSourceModel dataSourceModel = e.getValue();
            if (dataSourceModel.isShared())
            {
                List<String> apps = sharedDataSourceToApp.get(dataSourceName);
                if (apps == null)
                {
                    apps = new ArrayList<>();
                    sharedDataSourceToApp.put(dataSourceName, apps);
                }

                apps.add(appName);

                sharedDataSourceModels.compute(
                    dataSourceName,
                    (name,model) -> ModelMerger.merge(model, dataSourceModel)
                );
            }
        }
        return new AppArtifacts(resourceLoader, domainService, applicationModel, mergedStageModel, state, activeStageNames);
    }


    private List<String> getActiveStageNames(
        ApplicationModel applicationModel,
        AppState state
    )
    {
        final List<String> activeStageNames;
        final String stagesFromEnvProps = env.getProperty(ExceedProperties.STAGES_PROPERTY_PREFIX + applicationModel.getName());
        if (StringUtils.hasText(stagesFromEnvProps))
        {
            activeStageNames = Util.split(stagesFromEnvProps, ",");
        }
        else
        {
            activeStageNames = state.getStages();
        }

        return activeStageNames;
    }


    private static List<ResourceRoot> configureExtensions(
        Environment env, ServletContext servletContext, AppState state
    )
    {
        try
        {
            List<ResourceRoot> resourceRoots = new ArrayList<>();
            String appName = state.getName();
            String extensionPath = state.getPath();
            List<String> extensionNames = state.getExtensions();
            resourceRoots.add(getBaseExtension(appName));

            boolean extensionsInClasspath = extensionPath.startsWith("classpath:");

            for (String extension : extensionNames)
            {
                if (extensionsInClasspath)
                {
                    final String base = extensionPath.substring(CLASSPATH_PREFIX.length());
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

            final String path = env.getProperty(ExceedProperties.EXTRA_PROPERTY_PREFIX + state.getName());

            if (path != null)
            {
                File extraExtension = new File(path);
                if (!extraExtension.isDirectory())
                {
                    throw new IllegalStateException( "Extra extension path " + path + " is not a directory");
                }

                log.info("Using extra extension with path {}", path);

                resourceRoots.add(
                    new FileResourceRoot(
                        extraExtension,
                        true
                    )
                );
            }
            return resourceRoots;
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    private static ResourceRoot getBaseExtension(String appName)
    {
        File sourceDir = Util.getExceedLibrarySource();
        if (sourceDir != null)
        {
            File modelSourceLocation = new File(sourceDir, Util.toSlashPath("src/main/base")).getAbsoluteFile();

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


    private static StageModel mergeStages(
        Environment env, ApplicationModel applicationModel, AppState state,
        List<String> activeStageNames
    )
    {
        final Map<String, StageModel> stageModels = applicationModel.getStageModels();


        return activeStageNames.stream()
            .map(name -> stageModels.get(name))
            .reduce(null,ModelMerger::merge);
    }


    public Map<String, List<String>> getSharedDataSourceToApp()
    {
        return sharedDataSourceToApp;
    }


    private static class AppArtifacts
    {
        public final DefaultResourceLoader resourceLoader;
        public final DomainService domainService;
        public final ApplicationModel applicationModel;
        public final StageModel mergedStateModel;
        public final AppState initialAppState;
        public final List<String> activeStageNames;

        public AppArtifacts(
            DefaultResourceLoader resourceLoader,
            DomainService domainService,
            ApplicationModel applicationModel,
            StageModel mergedStateModel,
            AppState state,
            List<String> activeStageNames
        )
        {
            this.resourceLoader = resourceLoader;
            this.domainService = domainService;
            this.applicationModel = applicationModel;
            this.mergedStateModel = mergedStateModel;
            this.initialAppState = state;
            this.activeStageNames = activeStageNames;
        }
    }
}
