package de.quinscape.exceed.runtime.resource;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import de.quinscape.exceed.runtime.service.CachedResource;

public class DefaultResourceCacheFactory
    implements ResourceCacheFactory
{
    private int cacheSizePerApplication;

    /**
     * Sets the cache size in KB used for every running application instance.
     *
     * @param cacheSizePerApplication   application resource cache size in KB
     */
    public void setCacheSizePerApplication(int cacheSizePerApplication)
    {
        this.cacheSizePerApplication = cacheSizePerApplication;
    }


    @Override
    public LoadingCache<String,CachedResource> createCache(ResourceLoader resourceLoader)
    {
        return CacheBuilder.newBuilder()
            .maximumWeight(cacheSizePerApplication)
            .weigher(new CachedResourceWeigher())
            .build(new ResourceCacheLoader(resourceLoader));
    }
}
