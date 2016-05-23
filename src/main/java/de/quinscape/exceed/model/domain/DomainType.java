package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.annotation.IncludeDocs;
import de.quinscape.exceed.runtime.domain.DomainService;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DomainType
    extends TopLevelModel
{
    private List<String> pkFields = Collections.singletonList("id");

    private Set<String> pkFieldSet = new HashSet<>(pkFields);

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


    /**
     * List of properties for this domain type.
     * @return
     */
    @IncludeDocs
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
        if (pkFields == null)
        {
            throw new IllegalArgumentException("pkFields can't be null");
        }

        this.pkFields = pkFields;
        this.pkFieldSet = new HashSet<>(pkFields);
    }


    /**
      List of properties that form the primary key for this type.
     */
    @JSONTypeHint(String.class)
    public List<String> getPkFields()
    {
        return pkFields;
    }

    /**
     * DomainService
     * @return
     */
    @JSONProperty(ignore = true)
    public DomainService getDomainService()
    {
        return domainService;
    }


    public boolean isPKField(String name)
    {
        return pkFieldSet.contains(name);
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "pkFields = " + pkFields
            + ", properties = " + properties
            ;
    }
}
