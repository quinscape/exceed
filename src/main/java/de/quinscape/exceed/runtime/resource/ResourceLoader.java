package de.quinscape.exceed.runtime.resource;

import com.google.common.cache.LoadingCache;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.service.CachedResource;

import java.util.List;
import java.util.Map;

public interface ResourceLoader
{
    LoadingCache<String, CachedResource> getResourceCache();

    PathResources getResources(String relativePath);

    Map<String, PathResources> getAllResources();

    String readResource(String relativePath);

    long lastModified(String relativePath) throws ResourceNotFoundException;

    List<? extends ResourceRoot> getExtensions();
}
