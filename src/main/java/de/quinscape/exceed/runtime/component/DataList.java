package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumModel;
import de.quinscape.exceed.runtime.domain.DomainObject;
import org.svenson.JSONProperty;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataList
{
    private Map<String, DomainType> types;

    private Map<String, EnumModel> enums;

    private Map<String, ColumnDescriptor> columns;

    private List<?> rows;

    private int rowCount;

    private Object id;

    public DataList()
    {
        this(null, null, null, null, -1);
    }

    public DataList(Map<String, DomainType> types, Map<String, ColumnDescriptor> columns, List<?> rows, int rowCount)
    {
        this(types, columns, Collections.emptyMap(), rows, rowCount);
    }

    public DataList(Map<String, DomainType> types, Map<String, ColumnDescriptor> columns, Map<String, EnumModel> enums, List<?> rows, int rowCount)
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


    public Map<String, EnumModel> getEnums()
    {
        return enums;
    }


    public void setEnums(Map<String, EnumModel> enums)
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
}
