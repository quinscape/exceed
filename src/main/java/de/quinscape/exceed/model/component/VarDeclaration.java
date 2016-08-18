package de.quinscape.exceed.model.component;

import org.svenson.JSONParameter;

public class VarDeclaration
{
    private final String name;
    private final String type;

    public VarDeclaration(
        @JSONParameter("name")
        String name,
        @JSONParameter("type")
        String type
    )
    {
        this.type = type;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

}
