package de.quinscape.exceed.runtime.service.client;

import org.svenson.JSON;

/**
 * Result based on an object that is automatically converted to JSON with the default generator.
 * 
 */
public class DefaultClientData
    implements ClientData
{

    /**
     * A client state result of <code>null</code>.
     */
    public final static ClientData NONE = new DefaultClientData(null);

    private final String json;


    public DefaultClientData(Object data)
    {
        this.json = JSON.defaultJSON().forValue(data);
    }


    @Override
    public String getJSON()
    {
        return json;
    }
}
