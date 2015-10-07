package de.quinscape.exceed.model.routing;


import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import org.svenson.JSONProperty;

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
}
