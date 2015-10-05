package de.quinscape.exceed.model.routing;

import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.List;

public class Node
{
    private String name;

    private Mapping mapping;
    private List<Node> children;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Node> getChildren()
    {
        return children;
    }

    @JSONProperty("kids")
    @JSONTypeHint(Node.class)
    public void setChildren(List<Node> children)
    {
        this.children = children;
    }

    public Mapping getMapping()
    {
        return mapping;
    }

    public void setMapping(Mapping mapping)
    {
        this.mapping = mapping;
    }

    public boolean isVariable()
    {
        return name.startsWith("{") && name.endsWith("}");
    }
}
