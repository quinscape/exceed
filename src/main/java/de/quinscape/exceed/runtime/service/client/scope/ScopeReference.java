package de.quinscape.exceed.runtime.service.client.scope;

import com.google.common.base.Objects;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.util.Util;

public final class ScopeReference
{
    private final String name;

    private final ScopedPropertyModel model;

    private final Class<? extends ScopedContext> scopeType;


    public ScopeReference(String name, Class<? extends ScopedContext> scopeType, ScopedPropertyModel model)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        if (scopeType == null)
        {
            throw new IllegalArgumentException("scopeType can't be null");
        }

        if (model == null)
        {
            throw new IllegalArgumentException("model can't be null for non-reserved name '" + name + "'");
        }

        this.name = name;
        this.model = model;
        this.scopeType = scopeType;
    }

    public String getName()
    {
        return model.getName();
    }


    public ScopedPropertyModel getModel()
    {
        return model;
    }


    public Class<? extends ScopedContext> getScopeType()
    {
        return scopeType;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ScopeReference)
        {
            ScopeReference that = (ScopeReference) obj;

            // scope type shouldn't matter for equals/hashCode because the same name should always have the same type

            return this.name.equals(that.name) &&
                Objects.equal(this.model, that.model);

        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return Util.hashcodeOver(name, model);
    }
}
