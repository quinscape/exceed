package de.quinscape.exceed.model;

import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.runtime.resource.AppResource;
import org.svenson.JSONProperty;

/**
 * Base class for models with a name and a corresponding file name.
 *
 * Stores the original name internally to handle being renamed.
 *
 */
public abstract class AbstractTopLevelModel
    extends AbstractModel
    implements TopLevelModel
{
    private String name;

    private AppResource resource;

    private int extensionIndex;

    private String versionGUID;
    private String identityGUID;


    /**
     * Name of the top level model.
     * @return
     */
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    @JSONProperty(ignore = true)
    public void setResource(AppResource resource)
    {
        this.resource = resource;
    }


    /**
     * Resource this model was read from.
     * @return
     */
    @Override
    public final AppResource getResource()
    {
        return resource;
    }

    @Override
    @Internal
    public int getExtension()
    {
        return extensionIndex;
    }

    @Override
    public void setExtension(int extensionIndex)
    {
        this.extensionIndex = extensionIndex;
    }


    @Override
    @JSONProperty(priority = 10, ignoreIfNull = true)
    public String getVersionGUID()
    {
        return versionGUID;
    }


    @Override
    public void setVersionGUID(String versionGUID)
    {
        this.versionGUID = versionGUID;
    }


    @Override
    @JSONProperty(priority = 20, ignoreIfNull = true)
    public String getIdentityGUID()
    {
        return identityGUID;
    }


    @Override
    public void setIdentityGUID(String identityGUID)
    {
        this.identityGUID = identityGUID;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            ;
    }
}
