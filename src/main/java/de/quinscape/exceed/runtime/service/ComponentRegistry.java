package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.component.ComponentPackageDescriptor;
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
import org.springframework.beans.factory.annotation.Autowired;
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
public class ComponentRegistry
    implements ResourceChangeListener
{
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static Logger log = LoggerFactory.getLogger(ComponentRegistry.class);

    private JSONParser parser;

    @Autowired
    private StyleService styleService;

    @Autowired
    private ApplicationService applicationService;

    private ConcurrentMap<String,ComponentRegistration> components = new ConcurrentHashMap<>();
    private ConcurrentMap<String,Set<String>> packages = new ConcurrentHashMap<>();

    public ComponentRegistry()
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


    private void processResource(ResourceRoot root, AppResource appResource, boolean reload) throws IOException
    {
        if (!appResource.getRelativePath().endsWith(FileExtension.JSON))
        {
            return;
        }

        ComponentPackageDescriptor componentPackageDescriptor = parser.parse(ComponentPackageDescriptor.class, new String(appResource.read(), UTF8));

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

            ComponentRegistration registration = new ComponentRegistration(componentName, descriptor, styleSheetName, styles);
            components.put(componentName, registration);

            log.debug("(Re)registering {}", registration);

            componentNames.add(componentName);
        }

        packages.put(appResource.getRelativePath(), componentNames);
    }


    public ComponentRegistration getComponentRegistration(String name)
    {
        return components.get(name);
    }


    @Override
    public void onResourceChange(ModuleResourceEvent resourceEvent, FileResourceRoot root, String resourcePath)
    {
        /*
            we're only interested in JSON and CSS changes, the js changes get picked up by a running gulp watcher and
            when that regenerates the bundle it will be reloaded by
            {@link de.quinscape.exceed.runtime.controller.ScriptController}
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
                        Set<String> componentNames = packages.get(resourcePath);
                        if (componentNames != null)
                        {
                            componentNames.forEach(components::remove);
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
}
