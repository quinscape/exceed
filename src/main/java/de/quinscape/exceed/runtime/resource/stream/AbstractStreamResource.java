package de.quinscape.exceed.runtime.resource.stream;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.AppResource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractStreamResource
    implements AppResource
{
    protected final int extensionIndex;
    protected final String path;
    protected final String relative;

    public AbstractStreamResource(String path, int extensionIndex, String relative)
    {
        this.path = path;
        this.extensionIndex = extensionIndex;
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
            return IOUtils.toString(openStream(), "UTF-8");
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
            + "extensionIndex = " + extensionIndex
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
}
