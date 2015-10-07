package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.NamedModel;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Map;
import java.util.Set;

public class DomainType
    extends NamedModel
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
