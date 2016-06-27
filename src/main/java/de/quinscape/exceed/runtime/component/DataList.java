package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.svenson.JSONProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataList
{
    private Map<String, DomainProperty> columns;

    private List<?> rows;

    private int rowCount;

    private Object id;

    public DataList(Map<String, DomainProperty> columns, List<?> rows, int rowCount)
    {
        this.columns = columns;
        this.rows = rows;
        this.rowCount = rowCount;
    }



    public Map<String, DomainProperty> getColumns()
    {
        return columns;
    }


    public void setColumns(Map<String, DomainProperty> columns)
    {
        this.columns = columns;
    }


    public List<?> getRows()
    {
        return rows;
    }


    public void setRows(List<?> rows)
    {
        this.rows = rows;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + ", columns = " + columns
            + ", rows = " + rows
            ;
    }


    public void setRowCount(int rowCount)
    {
        this.rowCount = rowCount;
    }


    public int getRowCount()
    {
        return rowCount;
    }


    @JSONProperty(ignoreIfNull = true)
    public Object getId()
    {
        return id;
    }


    public void setId(Object id)
    {
        this.id = id;
    }


    /**
     * Returns a datalist with deep copied rows and shallow copied type information-
     *
     * @return
     */
    public DataList copy(RuntimeContext runtimeContext)
    {
        List copiedRows = new ArrayList<>(rows.size());

        for (Object o : rows)
        {
            Object copy;
            if (o instanceof DomainObject)
            {
                copy = DomainUtil.copy(runtimeContext, (DomainObject) o);
            }
            else if (o instanceof Map)
            {
                copy = new HashMap<>((Map)o);
            }
            else
            {
                throw new IllegalStateException("Don't know how to copy " + o);
            }

            copiedRows.add(copy);
        }
        return new DataList(columns, copiedRows, rowCount);
    }
}
