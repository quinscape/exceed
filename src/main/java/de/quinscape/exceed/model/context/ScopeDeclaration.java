package de.quinscape.exceed.model.context;

import de.quinscape.exceed.runtime.scope.ScopeType;
import org.svenson.JSONProperty;

import java.util.Objects;

public final class ScopeDeclaration
{
    private final String name;

    private final String scopeLocation;

    private final ScopeType scopeType;

    private final ScopedPropertyModel model;


    public ScopeDeclaration(String name, String scopeLocation, ScopeType scopeType, ScopedPropertyModel model)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        if (scopeLocation == null)
        {
            throw new IllegalArgumentException("scopeLocation can't be null");
        }

        if (scopeType == null)
        {
            throw new IllegalArgumentException("scopeType can't be null");
        }


        this.name = name;
        this.scopeLocation = scopeLocation;
        this.scopeType = scopeType;
        this.model = model;
    }

    public String getName()
    {
        return name;
    }


    @JSONProperty(ignore = true)
    public String getScopeLocation()
    {
        return scopeLocation;
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
                    this.scopeLocation.equals(that.scopeLocation) &&
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
        return Objects.hash(name, scopeLocation, scopeType);
    }


    @Override
    public String toString()
    {
        return "Scope definition '" + name + "' in " + scopeLocation + " (" + scopeType + ")";
    }

}
