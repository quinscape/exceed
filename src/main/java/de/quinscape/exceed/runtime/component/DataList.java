package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.expression.query.DataField;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Is a loosely typed set of data rows with a meta data block linking the fields of those rows by name to
 * either entity or model identities.
 *
 * This the java side of the primary data transport from the server to the client as JSON and back.
 *
 * The entities data lists are JSONified by {@link de.quinscape.exceed.runtime.datalist.DataListService.DataListJSONifier}
 */
public class DataList
{
//    private final List<IdentityDefinition> identities;
//
    private final static List<String> ID_PK_LIST = Collections.singletonList("id");

    private final Map<String, DataField> fields;

    private final Map<String, DomainType> types;
    private final List<?> rows;

    private final int rowCount;


    public DataList(
        @JSONParameter("types")
        @JSONTypeHint(DomainType.class)
        Map<String, DomainType> types,

        @JSONParameter("fields")
        Map<String, DataField> fields,

        @JSONParameter("rows")
        @JSONTypeHint(GenericDomainObject.class)
        List rows,

        @JSONParameter("rowCount")
        Integer rowCount
    )
    {
        this.types = types;
        this.fields = fields;
        this.rows = rows;
        this.rowCount = rowCount != null ? rowCount: 0;
    }

    public DataList(QueryDefinition queryDefinition, List<?> rows, int rowCount)
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
        this.types = collectDomainTypes(queryDefinition);
        this.fields = queryDefinition.getQueryDomainType().getFields();
    }


    /**
     * Creates a map mapping the query definition aliases to the type definitions for the query.
     *
     * @param queryDefinition   query definition
     *
     * @return map mapping the aliases to the type definitions
     */
    private Map<String, DomainType> collectDomainTypes(QueryDefinition queryDefinition)
    {
        Map<String, DomainType> map = new HashMap<>();

        QueryDomainType current = queryDefinition.getQueryDomainType();
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


    /**
     * Returns a map mapping the local name of a field to a data field definition.
     *
     * @return
     */
    public Map<String, DataField> getFields()
    {
        return fields;
    }


    /**
     * Returns a map mapping the local name of a type to the type definition.
     *
     * @return
     */
    public Map<String, DomainType> getTypes()
    {
        return types;
    }


    public List<?> getRows()
    {
        return rows;
    }


    public int getRowCount()
    {
        return rowCount;
    }

}
