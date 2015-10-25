package de.quinscape.exceed.runtime.resource;

import com.google.common.cache.LoadingCache;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import de.quinscape.exceed.runtime.service.CachedResource;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResourceLoader
    implements ResourceChangeListener
{
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ResourceLoader.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final List<ResourceRoot> extensions;
    private final ConcurrentMap<String, ResourceLocation> resourceLocations;

    private LoadingCache<String, CachedResource> resourceCache;


    public ResourceLoader(List<ResourceRoot> extensions)
    {
        this.extensions = extensions;

        resourceLocations = new ConcurrentHashMap<>();

        for (int extensionIndex = 0; extensionIndex < extensions.size(); extensionIndex++)
        {
            ResourceRoot resourceRoot = extensions.get(extensionIndex);

            resourceRoot.setExtensionIndex(extensionIndex);

            List<? extends AppResource> extensionResources = resourceRoot.listResources();

            for (AppResource resource : extensionResources)
            {
                String relative = resource.getRelativePath();

                ResourceLocation location = resourceLocations.get(relative);
                if (location == null)
                {
                    location = new ResourceLocation(relative);
                    resourceLocations.put(relative, location);
                }

                location.addExtensionResource(resource);
            }
        }
    }


    public void setResourceCache(LoadingCache<String, CachedResource> resourceCache)
    {
        if (resourceCache == null)
        {
            throw new IllegalArgumentException("cache can't be null");
        }

        this.resourceCache = resourceCache;

        for (ResourceRoot root : extensions)
        {
            ResourceWatcher watcher = root.getResourceWatcher();
            if (watcher != null)
            {
                watcher.register(this);
            }
        }
    }


    public LoadingCache<String, CachedResource> getResourceCache()
    {
        return resourceCache;
    }


    public ResourceLocation getResourceLocation(String relativePath)
    {
        return resourceLocations.get(relativePath);
    }


    public Map<String, ResourceLocation> getResourceLocations()
    {
        return resourceLocations;
    }

    public String readResource(String relativePath)
    {
        ResourceLocation resourceLocation = resourceFile(relativePath);
        return new String(resourceLocation.getHighestPriorityResource().read(), UTF8);
    }
    public long lastModified(String relativePath) throws ResourceNotFoundException
    {
        ResourceLocation resourceLocation = resourceFile(relativePath);
        return resourceLocation.getHighestPriorityResource().lastModified();
    }

    private ResourceLocation resourceFile(String relativePath)
    {
        ResourceLocation resourceLocation = getResourceLocation(relativePath);
        if (resourceLocation == null)
        {
            throw new ResourceNotFoundException("No resource found for relative path '" + relativePath + "'");
        }
        return resourceLocation;
    }

    public List<? extends ResourceRoot> getExtensions()
    {
        return extensions;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "extensions = " + extensions
            + ", resourceLocations = " + resourceLocations
            ;
    }


    @Override
    public void onResourceChange(ModuleResourceEvent resourceEvent, FileResourceRoot root, String resourcePath)
    {
        ResourceLocation resourceLocation = getResourceLocation(resourcePath);
        if (resourceLocation.getHighestPriorityResource().getResourceRoot().equals(root))
        {
            log.debug("Refresh cache for {}:{}", root, resourcePath);
            // we should only be registered if cache is not null
            resourceCache.refresh(resourcePath);
        }
    }
}
