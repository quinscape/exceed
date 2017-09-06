package de.quinscape.exceed.runtime.resource;

import com.google.common.cache.CacheLoader;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.service.CachedResource;

public class ResourceCacheLoader
    extends CacheLoader<String, CachedResource>
{
    private static final byte[] EMPTY = new byte[0];

    private final ResourceLoader resourceLoader;


    ResourceCacheLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }


    @Override
    public CachedResource load(String key) throws Exception
    {
        PathResources resourceLocation = resourceLoader.getResources(key);

        if (resourceLocation == null)
        {
            throw new ResourceNotFoundException("Resource " + key + " does not exist.");
        }

        AppResource resource = resourceLocation.getHighestPriorityResource();
        if (!resource.exists())
        {
            return new CachedResource(EMPTY);
        }

        return new CachedResource(resource.read());
    }
}
