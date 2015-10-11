package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.resource.AppResource;

import java.util.ArrayList;
import java.util.List;

public class ResourceLocation
{
    private final String relativePath;

    private List<AppResource> appResources = new ArrayList<>();

    public ResourceLocation(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public void addExtensionResource(AppResource appResource)
    {
        this.appResources.add(appResource);
    }

    public List<AppResource> getAppResources()
    {
        return appResources;
    }

    public AppResource getHighestPriorityResource()
    {
        return appResources.get(appResources.size() - 1);
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "relativePath = '" + relativePath + '\''
            + ", extensionResources = " + appResources
            ;
    }
}
