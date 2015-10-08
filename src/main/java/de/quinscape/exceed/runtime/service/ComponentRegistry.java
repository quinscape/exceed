package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.component.ComponentPackage;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.util.FileExtension;
import de.quinscape.exceed.runtime.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.svenson.JSONParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ComponentRegistry
{
    private JSONParser parser;

    @Autowired
    private StyleService styleService;

    private ConcurrentMap<String,ComponentRegistration> components = new ConcurrentHashMap<>();

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

        for (AppResource appResource : appResources)
        {
            if (!appResource.getRelativePath().endsWith(FileExtension.JSON))
            {
                continue;
            }

            ComponentPackage componentPackage = parser.parse(ComponentPackage.class, appResource.read());
            for (Map.Entry<String, ComponentDescriptor> entry : componentPackage.getComponents().entrySet())
            {
                String componentName = entry.getKey();
                ComponentDescriptor descriptor = entry.getValue();

                String parentDir = Util.parentDir(appResource.getRelativePath());


                String styles = null;
                AppResource resource = root.getResource(parentDir + "/" + componentName + FileExtension.CSS);
                if (resource.exists())
                {
                    String relativePath = resource.getRelativePath();

                    styles = styleService.process(root, resource.getRelativePath());
                }
                components.put(componentName,  new ComponentRegistration(descriptor, styles));
            }
        }
    }

    public ComponentRegistration getComponentRegistration(String name)
    {
        return components.get(name);
    }
}
