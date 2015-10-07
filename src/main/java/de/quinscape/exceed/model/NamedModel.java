package de.quinscape.exceed.model;

import org.svenson.JSONProperty;

/**
 * Base class for models with a name and a corresponding file name.
 *
 * Stores the original name internally to handle being renamed.
 *
 */
public abstract class NamedModel
    extends Model
{
    private String originalFilename;

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @JSONProperty(ignore = true)
    public void setFilename(String originalFilename)
    {
        this.originalFilename = originalFilename;
    }

    public final String getFilename()
    {
        if (originalFilename != null)
        {
            return originalFilename;
        }
        else
        {
            return fileNameForModel();
        }
    }

    protected String fileNameForModel()
    {
        return name + ".json";
    }

}
