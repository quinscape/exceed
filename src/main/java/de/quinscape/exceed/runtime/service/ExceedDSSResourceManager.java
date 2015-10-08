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
        try
        {
            return new ByteArrayInputStream(resourceRoot.getResource(name).read().getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    @Override
    public OutputStream getOutputStream(String name)
    {
        return null;
    }

    @Override
    public long getLastModified(String name)
    {
        return 0;
    }
}
