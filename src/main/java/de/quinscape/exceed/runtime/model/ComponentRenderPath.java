package de.quinscape.exceed.runtime.model;

public class ComponentRenderPath
{
    private final ComponentRenderPath parent;

    private String description;

    private String contextName;
    private int index;

    private String providedContext;

    private final String contentName;


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
    }


    public String getDescription()
    {
        return description;
    }


    public ComponentRenderPath getParent()
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


    public ComponentRenderPath firstChildPath(String name)
    {
        return new ComponentRenderPath(this, 0, name, contentName);
    }


    public String modelPath()
    {
        StringBuilder chain = new StringBuilder();

        ComponentRenderPath path = this;
        while (path.getParent() != null)
        {
            chain.insert(0, ".kids[" + path.getIndex() + "]");
            path = path.getParent();
        }

        return "_v.content." + contentName + chain;
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
