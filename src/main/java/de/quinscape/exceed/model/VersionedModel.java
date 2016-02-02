package de.quinscape.exceed.model;

import org.svenson.JSONProperty;

public interface VersionedModel
{
    @JSONProperty(ignoreIfNull = true)
    String getVersion();

    void setVersion(String version);
}
