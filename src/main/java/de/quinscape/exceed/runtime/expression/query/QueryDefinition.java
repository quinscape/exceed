package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.SimpleNode;

import java.util.List;

public class QueryDefinition
{
    private final QueryDomainType queryDomainType;
    private List<String> orderBy;

    private int limit = 20, offset = 0;

    private SimpleNode filter;


    public QueryDefinition(QueryDomainType queryDomainType)
    {
        this.queryDomainType = queryDomainType;
    }


    public QueryDomainType getQueryDomainType()
    {
        return queryDomainType;
    }


    public List<String> getOrderBy()
    {
        return orderBy;
    }


    public void setOrderBy(List<String> orderBy)
    {
        this.orderBy = orderBy;
    }


    public int getLimit()
    {
        return limit;
    }


    public void setLimit(int limit)
    {
        this.limit = limit;
    }


    public int getOffset()
    {
        return offset;
    }


    public void setOffset(int offset)
    {
        this.offset = offset;
    }


    public SimpleNode getFilter()
    {
        return filter;
    }


    public void setFilter(SimpleNode filter)
    {
        this.filter = filter;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "queryDomainType = " + queryDomainType
            + ", orderBy = " + orderBy
            + ", limit = " + limit
            + ", offset = " + offset
            + ", filter = " + filter
            ;
    }
}
