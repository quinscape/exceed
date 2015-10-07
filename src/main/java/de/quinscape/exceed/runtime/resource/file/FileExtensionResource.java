package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.ExtensionResource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileExtensionResource
    implements ExtensionResource
{
    private final int extensionIndex;
    private final File file;
    private final String relative;

    public FileExtensionResource(int extensionIndex, File file, String relative)
    {
        this.extensionIndex = extensionIndex;
        this.file = file;
        this.relative = relative;
    }

    @Override
    public int getExtensionIndex()
    {
        return extensionIndex;
    }

    @Override
    public String read()
    {
        try
        {
            return FileUtils.readFileToString(file, "UTF-8");
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    @Override
    public long lastModified()
    {
        return file.lastModified();
    }

    public File getFile()
    {
        return file;
    }

    @Override
    public String getRelativePath()
    {
        return relative;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "extensionIndex = " + extensionIndex
            + ", file = " + file
            ;
    }
}
