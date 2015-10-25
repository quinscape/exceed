package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import org.svenson.JSONProperty;

public class QueryField
    implements Cloneable
{
    private final QueryDomainType queryDomainType;

    private final DomainProperty domainProperty;

    private String localName;

    private String qualifiedName;

    private String nameFromStrategy;


    public QueryField(QueryDomainType queryDomainType, DomainProperty domainProperty)
    {
        this.queryDomainType = queryDomainType;
        this.domainProperty = domainProperty;

        qualifiedName = queryDomainType.getAlias() + "." + domainProperty.getName();
        localName = qualifiedName;
    }


    public QueryDomainType getQueryDomainType()
    {
        return queryDomainType;
    }


    public DomainProperty getDomainProperty()
    {
        return domainProperty;
    }


    @JSONProperty(ignore = true)
    public String getLocalName()
    {
        return localName;
    }


    @JSONProperty(ignore = true)
    public String getNameFromStrategy(NamingStrategy namingStrategy)
    {
        if (nameFromStrategy == null)
        {
            nameFromStrategy = namingStrategy.getFieldName(queryDomainType, domainProperty);
        }
        return nameFromStrategy;
    }


    public void setNameFromStrategy(String nameFromStrategy)
    {
        this.nameFromStrategy = nameFromStrategy;
    }

    public void setLocalName(String localName)
    {
        this.localName = localName;
    }

    @Override
    protected QueryField clone()
    {
        QueryField clone = null;
        try
        {
            clone = (QueryField) super.clone();
            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    public String getQualifiedName()
    {
        return qualifiedName;
    }
}
