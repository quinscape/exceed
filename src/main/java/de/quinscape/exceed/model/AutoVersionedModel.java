package de.quinscape.exceed.model;

import org.svenson.JSONProperty;

public interface AutoVersionedModel
{
    @JSONProperty(ignoreIfNull = true)
    String getVersion();

    void setVersion(String version);
}
