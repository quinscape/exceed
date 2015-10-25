package de.quinscape.exceed.runtime.resource;

import com.google.common.cache.CacheLoader;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import de.quinscape.exceed.runtime.service.CachedResource;

public class ResourceCacheLoader
    extends CacheLoader<String, CachedResource>
{
    private final ResourceLoader resourceLoader;


    ResourceCacheLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }


    @Override
    public CachedResource load(String key) throws Exception
    {
        ResourceLocation resourceLocation = resourceLoader.getResourceLocation(key);

        if (resourceLocation == null)
        {
            throw new IllegalStateException("Resource " + key + " does not exist.");
        }

        AppResource resource = resourceLocation.getHighestPriorityResource();
        if (!resource.exists())
        {
            throw new IllegalStateException("Resource " + key + " does not exist.");
        }

        return new CachedResource(resource.read());
    }
}
