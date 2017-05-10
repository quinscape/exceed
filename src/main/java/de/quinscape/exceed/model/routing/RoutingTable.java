package de.quinscape.exceed.model.routing;


import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.IncludeDocs;
import de.quinscape.exceed.model.annotation.DocumentedMapKey;
import de.quinscape.exceed.runtime.application.MappingNotFoundException;
import de.quinscape.exceed.runtime.application.RoutingResult;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class RoutingTable
    extends TopLevelModel
{
    private static final String STATE_ID_NAME = "{stateId?}";


    @Override
    @JSONProperty(priority = 80)
    public String getName()
    {
        return "routing";
    }


    private MappingNode rootNode;

    /**
     * Contains the mappings. Is a tree map to cause it being sorted by template location.
     */
    private TreeMap<String, Mapping> mappings;


    public TreeMap<String, Mapping> getMappings()
    {
        return mappings;
    }


    /**
     * The mappings for this routing table. Maps a route template string with potential
     * place holders to a mapping model.
     */
    @DocumentedMapKey("routeTemplate")
    @JSONProperty(priority = -10)
    @JSONTypeHint(Mapping.class)
    public void setMappings(TreeMap<String, Mapping> mappings)
    {

        final MappingNode node = new MappingNode();

        for (Map.Entry<String, Mapping> entry : mappings.entrySet())
        {
            final Mapping mapping = entry.getValue();

            final Boolean disabled = mapping.getDisabled();
            if (disabled == null || !disabled)
            {
                final String template = entry.getKey();
                final StringTokenizer tokenizer = new StringTokenizer(template, "/");
                insert(node, tokenizer, mapping, template);
            }
        }

        this.mappings = mappings;
        this.rootNode = node;
    }


    private void insert(MappingNode node, StringTokenizer tokenizer, Mapping mapping, String template)
    {
        if (!tokenizer.hasMoreTokens())
        {

            if (mapping.getProcessName() != null)
            {
                final ArrayList<MappingNode> kids = new ArrayList<>();
                final MappingNode stateIdNode = new MappingNode();
                stateIdNode.setName(STATE_ID_NAME);
                stateIdNode.setMapping(mapping);
                kids.add(stateIdNode);
                node.setChildren(kids);
            }
            else
            {
                node.setMapping(mapping);
            }
        }
        else
        {
            String token = tokenizer.nextToken();

            MappingNode newNode = new MappingNode();
            newNode.setName(token);

            List<MappingNode> children = node.getChildren();

            if (children == null)
            {
                children = new ArrayList<>();
                node.setChildren(children);
                children.add(newNode);
            }
            else
            {
                if (newNode.isVariable())
                {
                    for (MappingNode mappingNode : children)
                    {
                        if (mappingNode.isVariable() && !mappingNode.getVarName().equals(newNode.getVarName()))
                        {
                            throw new IllegalStateException("Mapping variables at the same position must have the " +
                                "same name. Conflict for '" + token + "' in " + template);
                        }
                    }

                    children.add(newNode);
                }
                else
                {
                    boolean found = false;
                    for (MappingNode mappingNode : children)
                    {
                        if (!mappingNode.isVariable() && mappingNode.getName().equals(token))
                        {
                            newNode = mappingNode;
                            found = true;
                            break;
                        }
                    }

                    if (!found)
                    {
                        children.add(newNode);
                    }
                }
            }

            insert(newNode, tokenizer, mapping, template);
        }
    }


    /**
     * Root mapping node.
     *
     * @return
     */
    public MappingNode getRootNode()
    {
        return rootNode;
    }


    @JSONProperty(ignore = true)
    public void setRootNode(MappingNode rootNode)
    {
        this.rootNode = rootNode;
    }


    public RoutingResult resolve(String path)
    {
        StringTokenizer tokenizer = new StringTokenizer(path, "/");

        MappingNode node = rootNode;

        Map<String, String> variables = new HashMap<>();

        StringBuilder buf = new StringBuilder();

        while (node != null && tokenizer.hasMoreTokens())
        {
            String part = tokenizer.nextToken();
            MappingNode found = null;
            MappingNode varCandidate = null;

            // make sure to first match all non-variables
            for (MappingNode kid : node.children())
            {
                if (kid.isVariable())
                {
                    varCandidate = kid;
                }
                else if (kid.getName().equals(part))
                {
                    found = kid;
                    buf.append("/").append(found.getName());
                    break;
                }
            }

            if (found == null && varCandidate != null)
            {
                found = varCandidate;
                buf.append("/").append(found.getName());
                variables.put(found.getVarName(), part);
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
        while (node.children().size() == 1)
        {
            node = node.getChildren().get(0);

            if (!node.isVariable() || node.isRequired())
            {
                return null;
            }
            buf.append("/").append(node.getName());
        }
        return !node.hasChildren() ? node.getMapping() : null;
    }


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        return visitor.visit(this, in);
    }
}

