package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.file.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public interface Extension
{
    void insertResources(ConcurrentMap<String, ResourceLocation> locations, int extensionIndex);

    boolean supportsHotReloading();
}
