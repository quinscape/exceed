package de.quinscape.exceed.runtime.resource.stream;

import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.stream.AbstractStreamResource;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import java.io.InputStream;

public class ServletResource
    extends AbstractStreamResource
{
    private final ServletContext servletContext;

    public ServletResource(ServletContext servletContext, ServletResourceRoot root, String relative, String path)
    {
        super(path, root, relative);

        this.servletContext = servletContext;
    }

    @Override
    protected InputStream openStream()
    {
        return servletContext.getResourceAsStream(path);
    }

}
