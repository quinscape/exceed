package de.quinscape.exceed.runtime.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComponentRenderPath
{
    private final ComponentRenderPath parent;

    private String description;

    private String contextName;

    private int index;

    private String providedContext;

    private final String contentName;
    private final Set<String> contextNames;


    public ComponentRenderPath(String contentName)
    {
        this( null, 0, "", contentName);
    }

    private ComponentRenderPath(ComponentRenderPath parent, int index, String description, String contentName)
    {
        this.parent = parent;
        this.index = index;
        this.description = description;
        this.contentName = contentName;

        final boolean isRoot = parent == null;
        this.contextNames = isRoot ? new HashSet<>() : null;
    }


    public String getDescription()
    {
        return description;
    }


    public ComponentRenderPath getParent()
    {
        return parent;
    }

    public ComponentRenderPath getRoot()
    {
        ComponentRenderPath path = this.getParent();
        ComponentRenderPath parent;
        while ((parent = path.getParent()) != null)
        {
            path = parent;
        }
        return path;
    }


    public int getIndex()
    {
        return index;
    }

    public void increment(String s)
    {
        this.index++;
        this.description = s;
        this.providedContext = null;
        this.contextName = null;
    }


    public String getContextName()
    {
        return contextName;
    }


    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }


    public String getUniqueName(String contextBase)
    {
        final boolean isRoot = parent == null;
        if (!isRoot)
        {
            return getRoot().getUniqueName(contextBase);
        }

        final String baseName = contextBase;
        int count = 2;

        while (contextNames.contains(contextBase))
        {
            contextBase = baseName + count++;
        }

        contextNames.add(contextBase);
        return contextBase;
    }


    public ComponentRenderPath firstChildPath(String name)
    {
        return new ComponentRenderPath(this, 0, name, contentName);
    }


    public String modelPath()
    {
        final String modelChain = modelChain();
        return "_v.model(" + modelChain + ")";
    }

    public String modelChain()
    {
        StringBuilder chain = new StringBuilder();

        ComponentRenderPath path = this;
        while (path.getParent() != null)
        {
            chain.insert(0, "," + path.getIndex());
            path = path.getParent();
        }

        return "[\"" + contentName + "\"" + chain + "]";
    }

    public List<Object> modelChainList()
    {

        ComponentRenderPath path = this;
        List<Object> chain = new ArrayList<>();
        while (path.getParent() != null)
        {
            chain.add(0, path.getIndex());
            path = path.getParent();
        }
        chain.add(0, contentName);
        return chain;
    }


    public void setProvidedContext(String providedContext)
    {
        this.providedContext = providedContext;
    }


    public String getProvidedContext()
    {
        return providedContext;
    }

    public ComponentRenderPath findContextByType( String type)
    {
        ComponentRenderPath path = this;
        while (path != null)
        {
            String contextName = path.getContextName();
            if (contextName != null && (type == null || type.equals(path.getProvidedContext())))
            {
                return path;
            }
            path = path.getParent();
        }
        return null;
    }
}
