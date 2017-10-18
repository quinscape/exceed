package de.quinscape.exceed.model.routing;

import de.quinscape.exceed.runtime.util.LocationUtil;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class MappingNode
{
    private String name;

    private String varName;

    private String navClass;

    private Mapping mapping;

    private List<MappingNode> children;

    private boolean required;

    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        final Matcher matcher = LocationUtil.match(name);
        if (matcher.matches())
        {
            varName = matcher.group(LocationUtil.VAR_NAME_GROUP);
            required = matcher.group(LocationUtil.VAR_OPTIONAL_GROUP) == null;
        }
        else
        {
            varName = null;
            required = false;
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


    @JSONProperty(readOnly = true)
    public boolean isRequired()
    {
        return required;
    }


    @JSONProperty(ignore = true)
    public String getVarName()
    {
        return varName;
    }


    public String getNavClass()
    {
        return navClass;
    }

    @JSONProperty("class")
    public void setNavClass(String navClass)
    {
        this.navClass = navClass;
    }
}
