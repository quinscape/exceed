package de.quinscape.exceed.runtime.editor.domain;

import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.message.Query;
import org.svenson.JSONParameter;

public class DomainTypeQuery
    extends Query
{
    public DomainTypeQuery(
        @JSONParameter("meta")
        MessageMeta meta
    )
    {
        super(meta);
    }
}
