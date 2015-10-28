package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.expression.query.DataField;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Is a loosely typed set of data rows with a meta data block linking the fields of those rows by name to
 * either entity or model identities.
 *
 * The entities data lists are JSONified by {@link de.quinscape.exceed.runtime.domain.DomainServiceImpl.DataListJSONifier}
 *
 * @param <T>   common type for all rows
 */
public class DataList<T>
{
    private final List<IdentityDefinition> identities;

    private final Map<String, DataField> fields;

    private final List<T> rows;

    private final int rowCount;


    public DataList(
        @JSONParameter("identities")
        @JSONTypeHint(IdentityDefinition.class)
        List<IdentityDefinition> identities,

        @JSONParameter("identities")
        @JSONTypeHint(DataField.class)
        Map<String, DataField> fields,

        @JSONParameter("rows")
        @JSONTypeHint(GenericDomainObject.class)
        List<T> rows,

        @JSONParameter("rowCount")
        Integer rowCount)
    {
        this.identities = identities;
        this.fields = fields;
        this.rows = rows;
        this.rowCount = rowCount != null ? rowCount: 0;
    }

    public DataList(QueryDefinition queryDefinition, List<T> rows, int rowCount)
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
        this.identities = createEntityDefinitions(queryDefinition);
        this.fields = queryDefinition.getQueryDomainType().getFields();

    }


    private List<IdentityDefinition> createEntityDefinitions(QueryDefinition queryDefinition)
    {
        List<IdentityDefinition> list = new ArrayList<>();

        QueryDomainType current = queryDefinition.getQueryDomainType();

        DataField dataField = null;
        do
        {
            list.add(new IdentityDefinition(current.getType().getName(), Collections.singletonList("id")));

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


    public List<T> getRows()
    {
        return rows;
    }


    public int getRowCount()
    {
        return rowCount;
    }


    public List<IdentityDefinition> getEntityDefinitions()
    {
        return identities;
    }


    public Map<String, DataField> getFields()
    {
        return fields;
    }
}
