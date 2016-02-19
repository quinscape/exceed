package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import org.svenson.AbstractDynamicProperties;
import org.svenson.JSONProperty;

public class GenericDomainObject
    extends AbstractDynamicProperties
    implements DomainObject
{

    private DomainService domainService;

    private String type;


    @Override
    @JSONProperty(ignore = true)
    public String getId()
    {
        return (String) getProperty("id");
    }


    @Override
    public void setId(String id)
    {
        setProperty("id", id);
    }

    @Override
    public DomainService getDomainService()
    {
        return domainService;
    }


    @Override
    @JSONProperty(ignore = true)
    public void setDomainService(DomainService domainService)
    {
        this.domainService = domainService;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    @Override
    @JSONProperty(value = "_type", priority = 100)
    public String getType()
    {
        return type;
    }

}
