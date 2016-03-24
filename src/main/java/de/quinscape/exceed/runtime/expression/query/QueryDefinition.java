package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumModel;
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


    public Map<String, ColumnDescriptor> createColumnDescriptorMap(RuntimeContext runtimeContext, Map<String, EnumModel> usedEnums)
    {
        Map<String, ColumnDescriptor> map = new HashMap<>();
        Map<String, DataField> fields = queryDomainType.getFields();

        Map<String, EnumModel> enums = runtimeContext.getRuntimeApplication().getApplicationModel().getEnums();

        for (Map.Entry<String, DataField> entry : fields.entrySet())
        {
            String localName = entry.getKey();
            DataField dataField = entry.getValue();

            DomainProperty domainProperty = dataField.getDomainProperty();

            map.put(localName, new ColumnDescriptor(
                dataField.getQueryDomainType().getType().getName(),
                domainProperty.getName()
            ));

            if (domainProperty.getType().equals("Enum"))
            {
                String name = (String) domainProperty.getTypeParam();

                EnumModel enumModel = enums.get(name);
                if (enumModel == null)
                {
                    throw new IllegalStateException("Reference to unknown enum '" + name + "'");
                }

                usedEnums.put(name, enumModel);
            }
        }

        return map;
    }
}
