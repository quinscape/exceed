package de.quinscape.exceed.runtime.service;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.component.ComponentPackageDescriptor;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.component.QueryDataProvider;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceChangeListener;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.util.FileExtension;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.svenson.JSONParser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ComponentRegistryImpl
    implements ResourceChangeListener, ApplicationContextAware, ComponentRegistry
{
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String DEFAULT_DATA_PROVIDER = "queryDataProvider";

    private final static Logger log = LoggerFactory.getLogger(ComponentRegistryImpl.class);

    private JSONParser parser;

    private StyleService styleService;

    @Autowired
    public void setStyleService(StyleService styleService)
    {
        this.styleService = styleService;
    }


    @Autowired
    private ApplicationService applicationService;

    private ApplicationContext applicationContext;

    private ConcurrentMap<String, ComponentRegistration> components = new ConcurrentHashMap<>();

    private ConcurrentMap<String, Set<String>> packages = new ConcurrentHashMap<>();

    private Map<String, DataProvider> dataProviders;


    public ComponentRegistryImpl()
    {
        this.parser = new JSONParser();
    }


    public void clear()
    {
        components.clear();
    }


    public void registerComponents(ResourceRoot root) throws IOException
    {
        List<? extends AppResource> appResources = root.listResources();

        ResourceWatcher resourceWatcher = root.getResourceWatcher();

        if (resourceWatcher != null)
        {
            resourceWatcher.register(this);
        }

        for (AppResource appResource : appResources)
        {
            processResource(root, appResource, false);
        }
    }


    private synchronized void processResource(ResourceRoot root, AppResource appResource, boolean reload) throws
        IOException
    {
        if (!appResource.getRelativePath().endsWith(FileExtension.JSON))
        {
            return;
        }

        ComponentPackageDescriptor componentPackageDescriptor;
        try
        {
            componentPackageDescriptor = parser.parse(
                ComponentPackageDescriptor.class,
                new String(appResource.read(), UTF8)
            );
        }
        catch (Exception e)
        {
            throw new ExceedRuntimeException("Error parsing component package descriptor from " + appResource, e);
        }

        //componentPackageDescriptor.setPath(appResource.getRelativePath());

        Set<String> componentNames = new HashSet<>();

        for (Map.Entry<String, ComponentDescriptor> entry : componentPackageDescriptor.getComponents().entrySet())
        {
            String componentName = entry.getKey();
            ComponentDescriptor descriptor = entry.getValue();

            String parentDir = Util.parentDir(appResource.getRelativePath());

            String styles = null;

            String styleSheetName = parentDir + "/" + componentName + FileExtension.CSS;
            AppResource resource = root.getResource(styleSheetName);
            if (resource.exists())
            {
                if (reload)
                {
                    styles = styleService.reload(root, resource.getRelativePath());
                }
                else
                {
                    styles = styleService.process(root, resource.getRelativePath());
                }
            }


            String dataProviderName = descriptor.getDataProviderName();
            DataProvider dataProviderBean = dataProviders.get(dataProviderName != null ? dataProviderName :
                DEFAULT_DATA_PROVIDER);
            if (dataProviderBean == null)
            {
                log.warn("No data provider with name {}", dataProviderName);
            }

            // if we have no queries, and the data provider is the query data provider, we ignore
            // it since it wouldn't provide as with data anyway and we can minimize the component models
            // we need to define id attributes for
            if (dataProviderBean instanceof QueryDataProvider && descriptor.getQueries().size() == 0)
            {
                dataProviderBean = null;
            }

            ComponentRegistration registration = new ComponentRegistration(componentName, descriptor,
                styles, dataProviderBean, getModuleName(parentDir, componentName));
            components.put(componentName, registration);

            log.debug("(Re)registering {}", registration);

            componentNames.add(componentName);
        }

        if (componentNames.size() > 0)
        {
            packages.put(appResource.getRelativePath(), componentNames);

            if (reload)
            {
                applicationService.signalComponentChanges(componentNames);
            }
        }
    }


    private String getModuleName(String parentDir, String componentName)
    {
        int pos = componentName.indexOf('.');
        if (pos >= 0)
        {
            componentName = componentName.substring(0, pos);
        }
        String result = "./components" + parentDir + "/" + componentName;

        log.debug("Module name for component {} is {}", componentName, result);

        return result;
    }


    @Override
    public ComponentRegistration getComponentRegistration(String name)
    {
        return components.get(name);
    }


    @Override
    public Set<String> getComponentNames()
    {
        return components.keySet();
    }

    @Override
    public void onResourceChange(ModuleResourceEvent resourceEvent, FileResourceRoot root, String resourcePath)
    {
        /*
            we're only interested in JSON and CSS changes, the js changes get picked up by a running gulp watcher and
            when that regenerates the bundle it will be cause a client notification for changed code which then will
            cause a reload via {@link de.quinscape.exceed.runtime.controller.ResourceController}
         */
        boolean isJSON = resourcePath.endsWith(FileExtension.JSON);
        if (isJSON || resourcePath.endsWith(FileExtension.CSS))
        {
            try
            {
                if (isJSON)
                {
                    if (resourceEvent == ModuleResourceEvent.DELETED)
                    {
                        synchronized (this)
                        {
                            Set<String> componentNames = packages.get(resourcePath);
                            if (componentNames != null)
                            {
                                componentNames.forEach(components::remove);
                                applicationService.signalComponentChanges(componentNames);
                            }
                        }
                    }
                    else
                    {
                        AppResource resource = root.getResource(resourcePath);
                        processResource(root, resource, true);
                    }
                }
                else
                {
                    AppResource resource = root.getResource(Util.parentDir(resourcePath) + "/component.json");
                    processResource(root, resource, true);
                }
                applicationService.signalStyleChanges();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;

        this.dataProviders = ImmutableMap.copyOf(this.applicationContext.getBeansOfType(DataProvider.class));

        log.info("DATA-PROVIDERS: {}", dataProviders);
    }
}
