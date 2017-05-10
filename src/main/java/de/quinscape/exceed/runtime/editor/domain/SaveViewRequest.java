package de.quinscape.exceed.runtime.editor.domain;

import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.message.Query;
import org.svenson.JSONParameter;

import java.util.Map;

public class SaveViewRequest
    extends Query
{
    private final Map<String, String> documents;


    public SaveViewRequest(
        @JSONParameter("documents")
        Map<String, String> documents,
        @JSONParameter("meta")
        MessageMeta meta
    )
    {
        super(meta);

        this.documents = documents;
    }


    public Map<String, String> getDocuments()
    {
        return documents;
    }
}
