package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.resource.AppResource;
import org.svenson.JSONProperty;

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
        List<AppResource> appResources1 = this.appResources;
        for (int i = 0; i < appResources1.size(); i++)
        {
            AppResource resource = appResources1.get(i);
            if (resource.getResourceRoot().getExtensionIndex() == appResource.getResourceRoot().getExtensionIndex())
            {
                appResources.set(i, appResource);
                return;
            }
        }
        
        this.appResources.add(appResource);
    }

    public List<AppResource> getAppResources()
    {
        return appResources;
    }

    @JSONProperty(ignore = true)
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
