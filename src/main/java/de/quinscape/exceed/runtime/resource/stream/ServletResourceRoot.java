package de.quinscape.exceed.runtime.resource.stream;

import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;

import javax.servlet.ServletContext;
import java.io.InputStream;

public class ServletResourceRoot
    extends AbstractStreamResourceRoot
{
    private final ServletContext servletContext;

    public ServletResourceRoot(ServletContext servletContext, String pathBase)
    {
        super(pathBase);

        this.servletContext = servletContext;
    }

    @Override
    protected InputStream openStream(String path)
    {
        return servletContext.getResourceAsStream(path);
    }

    @Override
    public AppResource getResource(String relative)
    {
        return new ServletResource(servletContext, this, relative, pathBase + relative);
    }


    @Override
    public ResourceWatcher getResourceWatcher()
    {
        return null;
    }

}
