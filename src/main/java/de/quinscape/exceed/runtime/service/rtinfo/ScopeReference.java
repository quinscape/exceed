package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.runtime.scope.ScopedValueType;

public class ScopeReference
{
    private final ScopedValueType type;

    private final String name;


    public ScopeReference(ScopedValueType type, String name)
    {
        this.type = type;
        this.name = name;
    }


    public ScopedValueType getType()
    {
        return type;
    }


    public String getName()
    {
        return name;
    }
}
