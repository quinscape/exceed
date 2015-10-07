package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.resource.ExtensionResource;

import java.util.ArrayList;
import java.util.List;

public class ResourceLocation
{
    private final String relativePath;

    private List<ExtensionResource> extensionResources = new ArrayList<>();

    public ResourceLocation(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public void addExtensionResource(ExtensionResource extensionResource)
    {
        this.extensionResources.add(extensionResource);
    }

    public List<ExtensionResource> getExtensionResources()
    {
        return extensionResources;
    }

    public ExtensionResource getHighestPriorityResource()
    {
        return extensionResources.get(extensionResources.size() - 1);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "relativePath = '" + relativePath + '\''
            + ", extensionResources = " + extensionResources
            ;
    }
}
