package de.quinscape.exceed.model.context;

import de.quinscape.exceed.runtime.scope.ScopeType;

import java.util.Objects;

public final class ScopeDeclaration
{
    private final ContextModel context;

    private final String name;

    private final String key;

    private final ScopeType scopeType;

    private final ScopedPropertyModel model;


    public ScopeDeclaration(ContextModel context, String name, String key, ScopeType scopeType, ScopedPropertyModel model)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("context can't be null");
        }

        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        if (key == null)
        {
            throw new IllegalArgumentException("key can't be null");
        }

        if (scopeType == null)
        {
            throw new IllegalArgumentException("scopeType can't be null");
        }


        this.context = context;
        this.name = name;
        this.key = key;
        this.scopeType = scopeType;
        this.model = model;
    }

    public ContextModel getContext()
    {
        return context;
    }


    public String getName()
    {
        return name;
    }


    public String getKey()
    {
        return key;
    }


    public ScopeType getScopeType()
    {
        return scopeType;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj instanceof ScopeDeclaration)
        {
            ScopeDeclaration that = (ScopeDeclaration) obj;

            return
                this.name.equals(that.name) &&
                    this.key.equals(that.key) &&
                    this.scopeType == that.scopeType;
        }
        return false;
    }

    public ScopedPropertyModel getModel()
    {
        return model;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, key, scopeType);
    }


    @Override
    public String toString()
    {
        return "Scope definition '" + name + "' in " + key + " (" + scopeType + ")";
    }

}
