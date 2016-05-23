package de.quinscape.exceed.model.routing;

import de.quinscape.exceed.model.annotation.IncludeDocs;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.List;

public class MappingNode
{
    private String name;

    private String varName;

    private Mapping mapping;

    private List<MappingNode> children;

    private boolean required;


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        if (name != null && name.startsWith("{") && name.endsWith("}"))
        {
            varName = name.substring(1, name.length() - 1).trim();
        }
        else
        {
            varName = null;
        }
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

    public boolean hasChildren()
    {
        return children != null && children().size() > 0;
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
        if (mapping != null)
        {
            boolean haveProcess = mapping.getProcessName() != null;
            boolean haveView = mapping.getViewName() != null;
            if (haveProcess == haveView)
            {
                throw new IllegalStateException("Need either a view reference or a process reference");
            }
        }
        this.mapping = mapping;
    }


    public boolean isVariable()
    {
        return varName != null;
    }


    public boolean isRequired()
    {
        return required;
    }


    public void setRequired(boolean required)
    {
        this.required = required;
    }


    @JSONProperty(ignore = true)
    public String getVarName()
    {
        return varName;
    }
}
