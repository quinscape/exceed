package de.quinscape.exceed.runtime.resource.stream;

import de.quinscape.exceed.runtime.resource.AppResource;

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
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }


    @Override
    public AppResource getResource(String resourcePath)
    {
        String path = pathBase + resourcePath;
        return new ClassPathResource(getExtensionIndex(), this.getClass().getClassLoader(), path, resourcePath);
    }
}
