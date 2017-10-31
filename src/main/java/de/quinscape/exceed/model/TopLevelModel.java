package de.quinscape.exceed.model;

import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.runtime.resource.AppResource;
import org.svenson.JSONProperty;

public interface TopLevelModel
    extends Model, AutoVersionedModel
{
    String getName();

    void setName(String name);

    @JSONProperty(ignore = true)
    void setResource(AppResource resource);

    AppResource getResource();

    @Internal
    int getExtension();

    void setExtension(int extensionIndex);

    <I,O> O accept(TopLevelModelVisitor<I, O> visitor, I in);
}
