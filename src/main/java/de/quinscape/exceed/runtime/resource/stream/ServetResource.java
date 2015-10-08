package de.quinscape.exceed.runtime.resource.stream;

import de.quinscape.exceed.runtime.resource.stream.AbstractStreamResource;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import java.io.InputStream;

public class ServetResource
    extends AbstractStreamResource
{
    private final ServletContext servletContext;

    public ServetResource(ServletContext servletContext, String relative, String path, int extensionIndex)
    {
        super(path, extensionIndex, relative);

        this.servletContext = servletContext;
    }

    @Override
    protected InputStream openStream()
    {
        return servletContext.getResourceAsStream(path);
    }

}
