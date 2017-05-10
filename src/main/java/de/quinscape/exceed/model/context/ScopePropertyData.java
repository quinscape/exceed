package de.quinscape.exceed.model.context;

import de.quinscape.exceed.runtime.scope.ScopedContext;

public class ScopePropertyData
{
    private final Class<? extends ScopedContext> scopeType;

    public ScopePropertyData(Class<? extends ScopedContext> scopeType)
    {
        this.scopeType = scopeType;
    }

    public Class<? extends ScopedContext> getScopeType()
    {
        return scopeType;
    }
}
