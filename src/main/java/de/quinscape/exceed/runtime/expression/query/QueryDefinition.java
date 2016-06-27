package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.model.domain.DomainProperty;
import org.jooq.Condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryDefinition
{
    private final QueryDomainType queryDomainType;

    private List<String> orderBy;

    private int limit = 20, offset = 0;

    private Condition filter;


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


    public Condition getFilter()
    {
        return filter;
    }


    public void setFilter(Condition filter)
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


    public Map<String, DomainProperty> createColumnDescriptorMap()
    {
        Map<String, DomainProperty> map = new HashMap<>();
        Map<String, DataField> fields = queryDomainType.getFields();

        for (Map.Entry<String, DataField> entry : fields.entrySet())
        {
            String localName = entry.getKey();
            DataField dataField = entry.getValue();

            DomainProperty domainProperty = dataField.getDomainProperty();

            map.put(localName, domainProperty);
        }

        return map;
    }
}
