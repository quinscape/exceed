package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.TopLevelModel;
import org.svenson.JSONTypeHint;

import java.util.Map;

public class DomainType
    extends TopLevelModel
{
    private Map<String,DomainProperty> properties;

    @JSONTypeHint(DomainProperty.class)
    public void setProperties(Map<String, DomainProperty> properties)
    {
        for (Map.Entry<String, DomainProperty> e : properties.entrySet())
        {
            e.getValue().setName(e.getKey());
        }

        this.properties = properties;
    }

    public Map<String, DomainProperty> getProperties()
    {
        return properties;
    }
}
