package de.quinscape.exceed.runtime.resource.file;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.util.Util;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * A file backed resource implementation.
 *
 */
public class FileAppResource
    implements AppResource
{
    private static final byte[] EMPTY = new byte[0];

    private final File file;
    private final String relative;

    private final ResourceRoot root;


    public FileAppResource(FileResourceRoot root, File file, String relative)
    {
        this.root = root;
        this.file = file;
        this.relative = Util.toSystemPath(relative);
    }


    @Override
    public ResourceRoot getResourceRoot()
    {
        return root;
    }


    @Override
    public byte[] read()
    {
        if (!file.exists())
        {
            return EMPTY;
        }

        try
        {
            return FileUtils.readFileToByteArray(file);
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
    public boolean delete()
    {
        return file.delete();
    }


    @Override
    public void write(byte[] bytes)
    {

        try
        {
            FileUtils.writeByteArrayToFile(file, bytes);
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    @Override
    public String toString()
    {
        return super.toString() + ": " + file.getPath();
    }
}
