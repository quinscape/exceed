package de.quinscape.exceed.runtime.editor.search;

import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.message.Query;
import org.svenson.JSONParameter;

public class SearchRequest
    extends Query
{

    private final String searchTerm;


    public SearchRequest(
        @JSONParameter("searchTerm")
            String searchTerm,
        @JSONParameter("meta")
            MessageMeta meta

    )
    {
        super(meta);
        this.searchTerm = searchTerm;
    }


    public String getSearchTerm()
    {
        return searchTerm;
    }
}
