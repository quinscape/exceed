package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.file.ResourceLocation;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates all resources in an application in all of its extensions.
 */
public class ApplicationResources
{
    private final List<? extends ResourceRoot> extensions;
    private final Map<String, ResourceLocation> resourceLocations;

    public ApplicationResources(List<? extends ResourceRoot> extensions, Map<String, ResourceLocation>
        resourceLocations)
    {
        this.extensions = extensions;
        this.resourceLocations =  resourceLocations;
    }

    public ResourceLocation getResourceLocation(String relativePath)
    {
        return resourceLocations.get(relativePath);
    }


    public Map<String, ResourceLocation> getResourceLocations()
    {
        return resourceLocations;
    }

    public String readResource(String relativePath)
    {
        ResourceLocation resourceLocation = resourceFile(relativePath);
        return resourceLocation.getHighestPriorityResource().read();
    }
    public long lastModified(String relativePath) throws ResourceNotFoundException
    {
        ResourceLocation resourceLocation = resourceFile(relativePath);
        return resourceLocation.getHighestPriorityResource().lastModified();
    }

    private ResourceLocation resourceFile(String relativePath)
    {
        ResourceLocation resourceLocation = getResourceLocation(relativePath);
        if (resourceLocation == null)
        {
            throw new ResourceNotFoundException("No resource found for relative path '" + relativePath + "'");
        }
        return resourceLocation;
    }

    public List<? extends ResourceRoot> getExtensions()
    {
        return extensions;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "extensions = " + extensions
            + ", resourceLocations = " + resourceLocations
            ;
    }
}
