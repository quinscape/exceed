package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.runtime.domain.DomainService;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.List;

public class DomainType
    extends TopLevelModel
{
    private List<String> pkFields = Collections.singletonList("id");

    /**
     * the linked hash map type ensures our properties stay in definition order
     */
    private List<DomainProperty> properties;

    private DomainService domainService;


    @JSONTypeHint(DomainProperty.class)
    public void setProperties(List<DomainProperty> properties)
    {
        this.properties = properties;
    }


    public List<DomainProperty> getProperties()
    {
        return properties;
    }


    public void setDomainService(DomainService domainService)
    {
        this.domainService = domainService;
    }


    public void setPkFields(List<String> pkFields)
    {
        this.pkFields = pkFields;
    }


    public List<String> getPkFields()
    {
        return pkFields;
    }


    @Override
    public String getName()
    {
        return super.getName();
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
