package de.quinscape.exceed.runtime.resource.stream;

import de.quinscape.exceed.runtime.resource.ResourceRoot;

import java.io.InputStream;

public class ClassPathResource
    extends AbstractStreamResource
{
    private final ClassLoader classLoader;

    public ClassPathResource(ClassPathResourceRoot root, ClassLoader classLoader, String path, String relative)
    {
        super(path, root, relative);

        this.classLoader = classLoader;
    }

    @Override
    protected InputStream openStream()
    {
        return classLoader.getResourceAsStream(path);
    }
}
