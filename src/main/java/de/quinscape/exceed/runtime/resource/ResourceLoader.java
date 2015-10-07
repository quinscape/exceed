package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.file.ResourceLocation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResourceLoader
{
    public ApplicationResources lookupResources(List<? extends ResourceRoot> extensions)
    {

        ConcurrentMap<String, ResourceLocation> locations = new ConcurrentHashMap<>();

        for (int extensionIndex = 0; extensionIndex < extensions.size(); extensionIndex++)
        {
            ResourceRoot resourceRoot = extensions.get(extensionIndex);
            resourceRoot.setExtensionIndex(extensionIndex);

            List<? extends ExtensionResource> extensionResources = resourceRoot.listResources();

            for (ExtensionResource resource : extensionResources)
            {
                String relative = resource.getRelativePath();

                ResourceLocation location = locations.get(relative);
                if (location == null)
                {
                    location = new ResourceLocation(relative);
                    locations.put(relative, location);
                }

                location.addExtensionResource(resource);
            }
        }

        return new ApplicationResources(extensions, locations);

    }
}
