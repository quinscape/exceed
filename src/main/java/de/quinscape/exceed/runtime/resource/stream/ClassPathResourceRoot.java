package de.quinscape.exceed.runtime.resource.stream;

import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceWatcher;
import de.quinscape.exceed.runtime.util.Util;

import java.io.InputStream;

public class ClassPathResourceRoot
    extends AbstractStreamResourceRoot
{

    public ClassPathResourceRoot(String classPathBase)
    {
        super(classPathBase);
    }


    @Override
    protected InputStream openStream(String path)
    {
        return this.getClass().getClassLoader().getResourceAsStream(Util.stripLeadingSlash(path));
    }


    @Override
    public AppResource getResource(String resourcePath)
    {
        String path = pathBase + resourcePath;
        return new ClassPathResource(this, this.getClass().getClassLoader(), path, resourcePath);
    }


    @Override
    public ResourceWatcher getResourceWatcher()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return pathBase;
    }

}
