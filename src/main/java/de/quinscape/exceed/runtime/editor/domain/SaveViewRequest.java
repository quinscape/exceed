package de.quinscape.exceed.runtime.editor.domain;

import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.message.Query;
import org.svenson.JSONParameter;

public class SaveViewRequest
    extends Query
{
    private final String viewName;

    private final String json;

    public SaveViewRequest(
        @JSONParameter("viewName")
        String viewName,
        @JSONParameter("document")
        String json,
        @JSONParameter("meta")
        MessageMeta meta
    )
    {
        super(meta);

        this.viewName = viewName;
        this.json = json;
    }


    public String getViewName()
    {
        return viewName;
    }


    public String getJson()
    {
        return json;
    }
}
