package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.ColumnDescriptor;
import org.jooq.Condition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


    public Map<String, DomainType> createDomainTypeMap()
    {
        Map<String, DomainType> map = new HashMap<>();

        QueryDomainType current = queryDomainType;
        do
        {
            DomainType type = current.getType();
            map.put(current.getAlias(), type);

            if (current.getJoinedType() != null)
            {
                current = current.getJoinedType().getRight();
            }
            else
            {
                current = null;
            }

        } while (current != null);

        return map;
    }


    public Map<String, ColumnDescriptor> createColumnDescriptorMap()
    {
        Map<String, ColumnDescriptor> map = new HashMap<>();
        Map<String, DataField> fields = queryDomainType.getFields();

        for (Map.Entry<String, DataField> entry : fields.entrySet())
        {
            String localName = entry.getKey();
            DataField dataField = entry.getValue();

            DomainProperty domainProperty = dataField.getDomainProperty();

            map.put(localName, new ColumnDescriptor(
                dataField.getQueryDomainType().getType().getName(),
                domainProperty.getName()
            ));
        }

        return map;
    }
}
