package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.file.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public interface ResourceRoot
{
    void setExtensionIndex(int extensionIndex);

    List<? extends ExtensionResource> listResources();

    boolean supportsHotReloading();
}
