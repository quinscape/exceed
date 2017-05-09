package de.quinscape.exceed.runtime.service.client;

/**
 * A result that has already been converted to JSON with potentially local conversion rules.
 */
public class JSONData
    implements ClientData
{

    private final String json;


    public JSONData(String json)
    {
        this.json = json;
    }


    @Override
    public String getJSON()
    {
        return json;
    }
}
