package de.quinscape.exceed.runtime.resource.classpath;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.ExtensionResource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class ClassPathExtensionResource
    implements ExtensionResource
{
    private final int extensionIndex;
    private final String relative;
    private final ClassLoader classLoader;

    public ClassPathExtensionResource(int extensionIndex, ClassLoader classLoader, String relative)
    {
        this.extensionIndex = extensionIndex;
        this.classLoader = classLoader;
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
            return IOUtils.toString(classLoader.getResourceAsStream(relative), "UTF-8");
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    @Override
    public long lastModified()
    {
        // no support
        return 0;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "extensionIndex = " + extensionIndex
            + ", relative = '" + relative + '\''
            ;
    }
}
