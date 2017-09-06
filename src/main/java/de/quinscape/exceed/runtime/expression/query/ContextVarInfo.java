package de.quinscape.exceed.runtime.expression.query;

public class ContextVarInfo
{
    private final String name;

    private final boolean isIterative;


    public ContextVarInfo(String name, boolean isIterative)
    {

        this.name = name;
        this.isIterative = isIterative;
    }


    public String getVarName()
    {
        return name;
    }


    public boolean isIterative()
    {
        return isIterative;
    }
}
