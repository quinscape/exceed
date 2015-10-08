package de.quinscape.exceed.runtime.resource.stream;

import java.io.InputStream;

public class ClassPathResource
    extends AbstractStreamResource
{
    private final ClassLoader classLoader;

    public ClassPathResource(int extensionIndex, ClassLoader classLoader, String path, String relative)
    {
        super(path, extensionIndex, relative);

        this.classLoader = classLoader;
    }

    @Override
    protected InputStream openStream()
    {
        return classLoader.getResourceAsStream(path);
    }
}
