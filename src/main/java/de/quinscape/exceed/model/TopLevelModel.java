package de.quinscape.exceed.model;

import de.quinscape.exceed.runtime.resource.AppResource;
import org.svenson.JSONProperty;

/**
 * Base class for models with a name and a corresponding file name.
 *
 * Stores the original name internally to handle being renamed.
 *
 */
public abstract class TopLevelModel
    extends Model
{
    private String name;

    private AppResource resource;

    private int extensionIndex;


    /**
     * Name of the top level model.
     * @return
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @JSONProperty(ignore = true)
    public void setResource(AppResource resource)
    {
        this.resource = resource;
    }


    /**
     * Resource this model was read from.
     * @return
     */
    public final AppResource getResource()
    {
        return resource;
    }

    public int getExtension()
    {
        return extensionIndex;
    }

    public void setExtension(int extensionIndex)
    {
        this.extensionIndex = extensionIndex;
    }
}
