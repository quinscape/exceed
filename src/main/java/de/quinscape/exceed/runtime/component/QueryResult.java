package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.expression.query.AmbiguousFieldReferenceException;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.expression.query.QueryField;
import org.svenson.JSONProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QueryResult
{
    private final List<EntityDefinition> entities;
    private final Map<String,QueryField> fields;
    private final List<? extends DomainObject> rows;
    private final int rowCount;

    public QueryResult(QueryDefinition queryDefinition, List<? extends DomainObject> rows, int rowCount)
    {
        if (queryDefinition == null)
        {
            throw new IllegalArgumentException("queryDefinition can't be null");
        }

        if (rows == null)
        {
            throw new IllegalArgumentException("rows can't be null");
        }

        this.rows = rows;
        this.rowCount = rowCount;
        this.entities = createEntityDefinitions(queryDefinition);
        this.fields = queryDefinition.getQueryDomainType().getFields();

    }


    private List<EntityDefinition> createEntityDefinitions(QueryDefinition queryDefinition)
    {
        List<EntityDefinition> list = new ArrayList<>();

        QueryDomainType current = queryDefinition.getQueryDomainType();

        QueryField queryField = null;
        do
        {
            list.add(new EntityDefinition(current.getType().getName(), Collections.singletonList("id")));

            if (current.getJoinedType() != null)
            {
                current = current.getJoinedType().getRight();
            }
            else
            {
                current = null;
            }

        } while (current != null);

        return list;
    }

    public List<? extends DomainObject> getRows()
    {
        return rows;
    }


    public int getRowCount()
    {
        return rowCount;
    }


    public List<EntityDefinition> getEntityDefinitions()
    {
        return entities;
    }


    public Map<String, QueryField> getFields()
    {
        return fields;
    }
}
