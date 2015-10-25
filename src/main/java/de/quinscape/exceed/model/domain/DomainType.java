package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.runtime.domain.DomainService;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.LinkedHashMap;
import java.util.Map;

public class DomainType
    extends TopLevelModel
{
    /**
     * the linked hash map type ensures our properties stay in definition order
     */
    private LinkedHashMap<String,DomainProperty> properties;

    private DomainService domainService;


    @JSONTypeHint(DomainProperty.class)
    public void setProperties(LinkedHashMap<String, DomainProperty> properties)
    {
        for (Map.Entry<String, DomainProperty> e : properties.entrySet())
        {
            e.getValue().setName(e.getKey());
        }

        this.properties = properties;
    }

    public LinkedHashMap<String, DomainProperty> getProperties()
    {
        return properties;
    }


    public void setDomainService(DomainService domainService)
    {
        this.domainService = domainService;
    }


    @JSONProperty(ignore = true)
    public DomainService getDomainService()
    {
        return domainService;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = " + getName()
            + "properties = " + properties
            ;
    }
}
