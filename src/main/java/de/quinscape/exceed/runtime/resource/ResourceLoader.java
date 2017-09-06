package de.quinscape.exceed.runtime.resource;

import com.google.common.cache.LoadingCache;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.ModuleResourceEvent;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.service.CachedResource;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class ResourceLoader
    implements ResourceChangeListener
{
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ResourceLoader.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final List<ResourceRoot> extensions;

    private final ConcurrentMap<String, PathResources> allResources;

    private LoadingCache<String, CachedResource> resourceCache;

    public ResourceLoader(List<ResourceRoot> extensions)
    {
        this.extensions = extensions;

        allResources = new ConcurrentHashMap<>();

        for (int extensionIndex = 0; extensionIndex < extensions.size(); extensionIndex++)
        {
            ResourceRoot resourceRoot = extensions.get(extensionIndex);

            resourceRoot.setExtensionIndex(extensionIndex);

            List<? extends AppResource> extensionResources = resourceRoot.listResources();

            for (AppResource resource : extensionResources)
            {
                String relative = resource.getRelativePath();

                PathResources pathResources = getResources(relative);
                if (pathResources == null)
                {
                    pathResources = register(resource);
                }
                pathResources.addExtensionResource(resource);
            }
        }
    }


    /**
     * Registers the given resource with a newly created ResourceLocation.
     * <p>
     * In general, we only register new resources on two occasions: At startup we register all existing resources and
     * if we do hot loading, we create new locations if we receive CREATED events about resources that did not exist
     * before. In all other cases, we don't do anything and return <code>null</code>.
     *
     * @param resource
     * @return
     */
    private PathResources register(AppResource resource)
    {
        final String relativePath = resource.getRelativePath();
        PathResources location = new PathResources(relativePath);
        allResources.put(relativePath, location);
        location.addExtensionResource(resource);
        return location;
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


    public PathResources getResources(String relativePath)
    {
        return allResources.get(Util.toSystemPath(relativePath));
    }


    /**
     * Returns a map containing all available app resources mapped by the relative path.
     *
     * @return map mapping relative paths to the path resources for that path.
     */
    public Map<String, PathResources> getAllResources()
    {
        return allResources;
    }


    public String readResource(String relativePath)
    {
        PathResources resourceLocation = resourceFile(relativePath);
        return new String(resourceLocation.getHighestPriorityResource().read(), UTF8);
    }


    public long lastModified(String relativePath) throws ResourceNotFoundException
    {
        PathResources resourceLocation = resourceFile(relativePath);
        return resourceLocation.getHighestPriorityResource().lastModified();
    }


    private PathResources resourceFile(String relativePath)
    {
        PathResources resourceLocation = getResources(relativePath);
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
            + ", allResources = " + allResources
            ;
    }


    @Override
    public void onResourceChange(ModuleResourceEvent resourceEvent, FileResourceRoot root, String resourcePath)
    {
        PathResources resourceLocation = getResources(resourcePath);
        if (resourceLocation == null)
        {
            resourceLocation = register(root.getResource(resourcePath));
        }

        if (resourceLocation.getHighestPriorityResource().getResourceRoot().equals(root))
        {
            log.debug("Refresh cache for {}:{}", root, resourcePath);
            if (resourceCache == null)
            {
                throw new IllegalStateException("No resource cache configured");
            }

            resourceCache.refresh(resourcePath);
        }
    }
}
