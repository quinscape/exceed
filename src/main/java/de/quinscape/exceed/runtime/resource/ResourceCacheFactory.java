package de.quinscape.exceed.runtime.resource;

import com.google.common.cache.LoadingCache;
import de.quinscape.exceed.runtime.service.CachedResource;

/**
 * Created by sven on 17.10.15.
 */
public interface ResourceCacheFactory
{
    LoadingCache<String,CachedResource> createCache(ResourceLoader resourceLoader);
}
