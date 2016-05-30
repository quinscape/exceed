package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.DomainObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates all existing scopes for an execution context.
 *
 * This will usually be either [app,session] or [app,session,process] contexts.
 */
public final class ScopedContextChain
    implements ScopedResolver
{
    private final List<ScopedContext> chainedScopes;

    public ScopedContextChain(List<ScopedContext> chainedScopes)
    {
        if (chainedScopes == null)
        {
            throw new IllegalArgumentException("chainedScopes can't be null");
        }

        this.chainedScopes = new ArrayList<>(chainedScopes);
    }


    @Override
    public Object getProperty(String name)
    {
        ScopedContext scopedContext = findScopeWithProperty(name);
        if (scopedContext == null)
        {
            throw new ScopeResolutionException("No property '" + name + "' in either " + describe());
        }

        return scopedContext.getProperty(name);
    }



    @Override
    public DomainObject getObject(String name)
    {
        ScopedContext scopedContext = findScopeWithObject(name);
        if (scopedContext == null)
        {
            throw new ScopeResolutionException("No object '" + name + "' in either " + describe());
        }

        return scopedContext.getObject(name);
    }


    @Override
    public DataList getList(String name)
    {
        ScopedContext scopedContext = findScopeWithList(name);
        if (scopedContext == null)
        {
            throw new ScopeResolutionException("No list '" + name + "' in either " + describe());
        }

        return scopedContext.getList(name);
    }


    @Override
    public void setProperty(String name, Object value)
    {
        ScopedContext scopedContext = findScopeWithProperty(name);
        if (scopedContext == null)
        {
            throw new ScopeResolutionException("No property '" + name + "' in either " + describe());
        }

        scopedContext.setProperty(name, value);
    }


    @Override
    public void setObject(String name, DomainObject value)
    {
        ScopedContext scopedContext = findScopeWithProperty(name);
        if (scopedContext == null)
        {
            throw new ScopeResolutionException("No object '" + name + "' in either " + describe());
        }

        scopedContext.setObject(name, value);
    }


    @Override
    public void setList(String name, DataList list)
    {
        ScopedContext scopedContext = findScopeWithProperty(name);
        if (scopedContext == null)
        {
            throw new ScopeResolutionException("No list '" + name + "' in either " + describe());
        }

        scopedContext.setList(name, list);
    }


    @Override
    public boolean hasProperty(String name)
    {
        return findScopeWithProperty(name) != null;
    }


    @Override
    public boolean hasObject(String name)
    {
        return findScopeWithObject(name) != null;
    }


    @Override
    public boolean hasList(String name)
    {
        return findScopeWithList(name) != null;
    }

    public ScopedContext findScopeWithProperty(String name)
    {
        for (int i = chainedScopes.size() - 1; i >= 0; i--)
        {
            ScopedContext scopedContext = chainedScopes.get(i);
            if (scopedContext.hasProperty(name))
            {
                return scopedContext;
            }
        }
        return null;
    }


    public ScopedContext findScopeWithObject(String name)
    {
        for (int i = chainedScopes.size() - 1; i >= 0; i--)
        {
            ScopedContext scopedContext = chainedScopes.get(i);
            if (scopedContext.hasObject(name))
            {
                return scopedContext;
            }
        }
        return null;
    }


    public ScopedContext findScopeWithList(String name)
    {
        for (int i = chainedScopes.size() - 1; i >= 0; i--)
        {
            ScopedContext scopedContext = chainedScopes.get(i);
            if (scopedContext.hasList(name))
            {
                return scopedContext;
            }
        }
        return null;
    }


    private String describe()
    {
        StringBuilder sb = new StringBuilder();
        for (Iterator<ScopedContext> iterator = chainedScopes.iterator(); iterator.hasNext(); )
        {
            ScopedContext ctx = iterator.next();
            sb.append(ctx.getClass().getSimpleName());

            if (iterator.hasNext())
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }


    /**
     * Adds an additional scoped context to the top of the scoped context chain.
     *
     * @param scopedContext scoped context
     */
    public void addContext(ScopedContext scopedContext)
    {
        this.chainedScopes.add(scopedContext);
    }
}
