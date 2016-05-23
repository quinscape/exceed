package de.quinscape.exceed.model.routing;


import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.annotation.IncludeDocs;
import de.quinscape.exceed.runtime.application.MappingNotFoundException;
import de.quinscape.exceed.runtime.application.RoutingResult;
import org.svenson.JSONProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class RoutingTable
    extends TopLevelModel
{
    @Override
    @JSONProperty(priority = 80)
    public String getName()
    {
        return "routing";
    }


    private MappingNode rootNode;


    /**
     * Root mapping node.
     * @return
     */
    @IncludeDocs
    public MappingNode getRootNode()
    {
        return rootNode;
    }


    public void setRootNode(MappingNode rootNode)
    {
        this.rootNode = rootNode;
    }


    public RoutingResult resolve(String path)
    {
        StringTokenizer tokenizer = new StringTokenizer(path, "/");

        MappingNode node = rootNode;

        Map<String,String> variables = new HashMap<>();

        StringBuilder buf = new StringBuilder();

        while (node != null && tokenizer.hasMoreTokens())
        {
            String part = tokenizer.nextToken();
            MappingNode found = null;
            for (MappingNode kid : node.children())
            {
                if (kid.getName().equals(part))
                {
                    found = kid;
                    buf.append("/").append(found.getName());
                    break;
                }
                if (kid.isVariable())
                {
                    found = kid;
                    buf.append("/").append(found.getName());
                    variables.put(kid.getVarName(), part);
                    break;
                }
            }
            node = found;
        }

        if (!tokenizer.hasMoreTokens() && node != null)
        {
            Mapping mapping = node.getMapping();
            if (mapping != null)
            {
                return new RoutingResult(mapping, variables, buf.toString());
            }
            else
            {
                mapping = followNonRequiredVars(node, buf);
                if (mapping != null)
                {
                    return new RoutingResult(mapping, variables, buf.toString());
                }
            }
        }

        throw new MappingNotFoundException("Could not find a valid mapping for path '" + path + "'");
    }


    private Mapping followNonRequiredVars(MappingNode node, StringBuilder buf)
    {
        if (node.children().size() == 1)
        {
            node = node.getChildren().get(0);
            buf.append("/").append(node.getName());
            while (node.children().size() == 1 && node.isVariable() && !node.isRequired())
            {
                node = node.getChildren().get(0);
                buf.append("/").append(node.getName());
            }
        }
        return !node.hasChildren() ? node.getMapping() : null;
    }
}

