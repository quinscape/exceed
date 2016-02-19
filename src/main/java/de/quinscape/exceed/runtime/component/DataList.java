package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.domain.DomainType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DataList
{
    private Map<String, DomainType> types;

    private Map<String, ColumnDescriptor> columns;

    private List<?> rows;


    public DataList()
    {
        this(null, null, null);
    }

    public DataList(Map<String, DomainType> types, Map<String, ColumnDescriptor> columns, List<?> rows)
    {
        this.types = types;
        this.columns = columns;
        this.rows = rows;
    }


    public Map<String, DomainType> getTypes()
    {
        return types;
    }


    public void setTypes(Map<String, DomainType> types)
    {
        this.types = types;
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
}
