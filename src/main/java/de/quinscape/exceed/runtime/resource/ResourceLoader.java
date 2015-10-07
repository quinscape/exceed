package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.file.ResourceLocation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResourceLoader
{
    public ApplicationResources lookupResources(List<? extends Extension> extensions)
    {

        ConcurrentMap<String, ResourceLocation> locations = new ConcurrentHashMap<>();

        for (int extensionIndex = 0; extensionIndex < extensions.size(); extensionIndex++)
        {
            Extension extension = extensions.get(extensionIndex);
            extension.insertResources(locations, extensionIndex);
        }

        return new ApplicationResources(extensions, locations);

    }
}
