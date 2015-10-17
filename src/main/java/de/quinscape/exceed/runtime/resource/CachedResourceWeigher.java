package de.quinscape.exceed.runtime.resource;

import com.google.common.cache.Weigher;
import de.quinscape.exceed.runtime.service.CachedResource;

public class CachedResourceWeigher
    implements Weigher<String, CachedResource>
{

    @Override
    public int weigh(String key, CachedResource cachedResource)
    {
        // weight is Kilobytes
        return cachedResource.getData().length / 1024;
    }
}
