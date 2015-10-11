package de.quinscape.exceed.runtime.service;

import de.quinscape.dss.DSSResourceManager;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.resource.ResourceRoot;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class ExceedDSSResourceManager
    implements DSSResourceManager
{
    private ResourceRoot resourceRoot;

    public void setResourceRoot(ResourceRoot resourceRoot)
    {
        this.resourceRoot = resourceRoot;
    }

    @Override
    public InputStream getInputStream(String name)
    {
        return new ByteArrayInputStream(resourceRoot.getResource(name).read());
    }

    @Override
    public OutputStream getOutputStream(String name)
    {
        return null;
    }

    @Override
    public long getLastModified(String name)
    {
        return resourceRoot.getResource(name).lastModified();
    }
}
