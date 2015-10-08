package de.quinscape.exceed.component;

import org.svenson.JSONParameter;

public class PropDeclaration
{
    private final String name;
    private final String type;

    public PropDeclaration(
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
