package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.component.QueryPreparationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryDefinition
{
    public static String QUERY_NAME_CONFIG = "queryName";

    private final QueryDomainType queryDomainType;

    private List<String> orderBy;

    private int limit = 0, offset = 0;

    private QueryCondition filter;

    private Map<String,Object> parameters = new HashMap<>();

    public QueryDefinition(QueryDomainType queryDomainType)
    {
        if (queryDomainType == null)
        {
            throw new IllegalArgumentException("queryDomainType can't be null");
        }

        this.queryDomainType = queryDomainType.getLeftMost().resolve();
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


    public QueryCondition getFilter()
    {
        return filter;
    }


    public void setFilter(QueryCondition filter)
    {
        this.filter = filter;
    }


    public void setParameter(String name, Object object)
    {
        parameters.put(name, object);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }


    public void setParameters(Map<String, Object> parameters)
    {
        this.parameters = parameters;
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

        for (String name : queryDomainType.getOriginalFields())
        {
            final QueryMapping mapping = queryDomainType.getQueryMappings().get(name);
            if (mapping != null)
            {
                map.put(
                    name,
                    DomainProperty.builder()
                        .withType(PropertyType.DOMAIN_TYPE, mapping.getDomainType())
                        .withConfig(QUERY_NAME_CONFIG, mapping.getQueryName())
                        .build()
                );
            }
            else
            {
                DomainProperty domainProperty = resolveColumnProperty(name);

                if (domainProperty != null)
                {
                    map.put(name, domainProperty);
                }
                else
                {
                    throw new QueryPreparationException("Could not resolve selected column '" + name + "'");
                }
            }
        }

        QueryDomainType current = queryDomainType;
        while (current != null)
        {
            final JoinDefinition joinDefinition = current.getJoinedType();
            if (joinDefinition != null)
            {
                for (DataField dataField : joinDefinition.getCondition().getFieldReferences())
                {
                    if (!dataField.getDomainProperty().getName().equals(DomainType.ID_PROPERTY))
                    {
                        map.put(
                            dataField.getQualifiedName(),
                            DomainProperty.builder()
                                .fromProperty(dataField.getDomainProperty())
                                .withConfig(QUERY_NAME_CONFIG, dataField.getQueryDomainType().getNameOrAlias())
                                .build()
                        );
                    }
                }
            }
            current = QueryDomainType.nextJoinedType(current);
        }

        return map;
    }


    private DomainProperty resolveColumnProperty(String name)
    {
        String prefix = null;
        String field = name;
        int dotPos = field.indexOf('.');
        if (dotPos >= 0)
        {
            prefix = field.substring(0, dotPos);
            field = field.substring(dotPos + 1);
        }

        DomainProperty domainProperty = null;
        for (DataField dataField : queryDomainType.getJoinedFields())
        {
            if (QueryDomainType.matches(dataField.getDomainProperty(), dataField.getQueryDomainType().getNameOrAlias(), prefix, field))
            {
                domainProperty = DomainProperty.builder()
                    .fromProperty(dataField.getDomainProperty())
                    .withConfig(QUERY_NAME_CONFIG, dataField.getQueryDomainType().getNameOrAlias())
                    .build();

                break;
            }
        }
        return domainProperty;
    }
}
