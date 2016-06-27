package de.quinscape.exceed.runtime.model;

public class ComponentPath
{
    private final ComponentPath parent;

    private String description;

    private String contextName;
    private int index;

    private String providedContext;


    public ComponentPath()
    {
        this( null, 0, "");
    }

    private ComponentPath(ComponentPath parent, int index, String description)
    {
        this.parent = parent;
        this.index = index;
        this.description = description;
    }


    public String getDescription()
    {
        return description;
    }


    public ComponentPath getParent()
    {
        return parent;
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
    }


    public String getContextName()
    {
        return contextName;
    }


    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }


    public ComponentPath firstChildPath(String name)
    {
        return new ComponentPath(this, 0, name);
    }


    public String modelPath()
    {
        StringBuilder chain = new StringBuilder();

        ComponentPath path = this;
        while (path.getParent() != null)
        {
            chain.insert(0, ".kids[" + path.getIndex() + "]");
            path = path.getParent();
        }

        return "_v.root" + chain;
    }


    public void setProvidedContext(String providedContext)
    {
        this.providedContext = providedContext;
    }


    public String getProvidedContext()
    {
        return providedContext;
    }
}
