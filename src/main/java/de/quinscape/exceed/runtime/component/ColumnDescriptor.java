package de.quinscape.exceed.runtime.component;

import org.svenson.JSONParameter;
import org.svenson.JSONProperty;

public class ColumnDescriptor
{
    private final String type;

    private final String name;


    public ColumnDescriptor()
    {
        this(null, null);
    }


    public ColumnDescriptor(
        @JSONParameter("type")
        String type,
        @JSONParameter("name")
        String name
    )
    {
        this.type = type;
        this.name = name;
    }


    @JSONProperty(priority = 1)
    public String getType()
    {
        return type;
    }


    public String getName()
    {
        return name;
    }

}
