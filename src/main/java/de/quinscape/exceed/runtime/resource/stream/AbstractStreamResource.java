package de.quinscape.exceed.runtime.resource.stream;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractStreamResource
    implements AppResource
{
    protected final String path;
    protected final String relative;

    private final ResourceRoot root;


    public AbstractStreamResource(String path, ResourceRoot root, String relative)
    {
        this.path = path;
        this.root = root;
        this.relative = relative;
    }


    @Override
    public byte[] read()
    {
        try
        {
            InputStream input = openStream();
            if (input == null)
            {
                throw new ExceedRuntimeException("Cannot read " + this);
            }


            return IOUtils.toByteArray(input);
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    protected abstract InputStream openStream();

    @Override
    public long lastModified()
    {
        // no support
        return 0;
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
            + "root = " + root
            + ", relative = '" + path + '\''
            ;
    }

    @Override
    public boolean exists()
    {
        InputStream is = openStream();
        IOUtils.closeQuietly(is);
        return is != null;
    }


    @Override
    public ResourceRoot getResourceRoot()
    {
        return root;
    }


    @Override
    public boolean delete()
    {
        throw new UnsupportedOperationException("Stream resource cannot support delete");
    }


    @Override
    public void write(byte[] bytes)
    {
        throw new UnsupportedOperationException("Stream resource cannot support write");
    }
}
