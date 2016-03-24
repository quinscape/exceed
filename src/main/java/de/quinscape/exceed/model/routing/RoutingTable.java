package de.quinscape.exceed.model.routing;


import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
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

        while (node != null && tokenizer.hasMoreTokens())
        {
            String part = tokenizer.nextToken();
            MappingNode found = null;
            MappingNode varNode = null;
            for (MappingNode kid : node.children())
            {
                if (kid.getName().equals(part))
                {
                    found = kid;
                    break;
                }
                if (kid.isVariable())
                {
                    found = kid;
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
                return new RoutingResult(mapping, variables);
            }
        }

        throw new MappingNotFoundException("Could not find a valid mapping for path '" + path + "'");
    }
}
