package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.runtime.domain.DomainObject;
import org.svenson.JSONProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataList
{
    private Map<String, DomainType> types;

    private Map<String, EnumType> enums;

    private Map<String, ColumnDescriptor> columns;

    private List<?> rows;

    private int rowCount;

    private Object id;

    public DataList(Map<String, DomainType> types, Map<String, ColumnDescriptor> columns, Map<String, EnumType> enums, List<?> rows, int rowCount)
    {
        this.types = types;
        this.enums = enums;
        this.columns = columns;
        this.rows = rows;
        this.rowCount = rowCount;
    }


    public Map<String, DomainType> getTypes()
    {
        return types;
    }


    public void setTypes(Map<String, DomainType> types)
    {
        this.types = types;
    }


    public Map<String, EnumType> getEnums()
    {
        return enums;
    }


    public void setEnums(Map<String, EnumType> enums)
    {
        this.enums = enums;
    }


    public Map<String, ColumnDescriptor> getColumns()
    {
        return columns;
    }


    public void setColumns(Map<String, ColumnDescriptor> columns)
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
            + "types = " + types
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
    public DataList copy()
    {
        List copiedRows = new ArrayList<>(rows.size());

        for (Object o : rows)
        {
            Object copy;
            if (o instanceof DomainObject)
            {
                copy = ((DomainObject) o).copy();
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
        return new DataList(types, columns, enums, copiedRows, rowCount);
    }
}
