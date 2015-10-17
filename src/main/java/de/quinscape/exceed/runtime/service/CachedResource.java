package de.quinscape.exceed.runtime.service;

import java.util.UUID;

public class CachedResource
{
    private final byte[] data;

    private final String id;


    public CachedResource(byte[] data)
    {
        this.data = data;
        this.id = "\"" + UUID.randomUUID().toString() + "\"";
    }


    public byte[] getData()
    {
        return data;
    }


    public String getId()
    {
        return id;
    }
}
