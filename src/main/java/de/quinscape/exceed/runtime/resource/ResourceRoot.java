package de.quinscape.exceed.runtime.resource;

import java.util.List;

public interface ResourceRoot
{
    void setExtensionIndex(int extensionIndex);

    int getExtensionIndex();

    List<? extends AppResource> listResources();

    boolean supportsHotReloading();

    AppResource getResource(String path);
}
