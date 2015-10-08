package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.AppResource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileAppResource
    implements AppResource
{
    private final int extensionIndex;
    private final File file;
    private final String relative;

    public FileAppResource(int extensionIndex, File file, String relative)
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

    @Override
    public boolean exists()
    {
        return file.exists();
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
