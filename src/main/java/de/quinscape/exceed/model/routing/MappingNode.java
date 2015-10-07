package de.quinscape.exceed.model.routing;

import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.List;

public class MappingNode
{
    private String name;

    private Mapping mapping;
    private List<MappingNode> children;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<MappingNode> children()
    {
        if (children == null)
        {
            return Collections.emptyList();
        }

        return children;
    }
    public List<MappingNode> getChildren()
    {
        return children;
    }

    @JSONProperty("kids")
    @JSONTypeHint(MappingNode.class)
    public void setChildren(List<MappingNode> children)
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
