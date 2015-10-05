package de.quinscape.exceed.model.routing;


import de.quinscape.exceed.model.ModelBase;
import de.quinscape.exceed.model.routing.Node;

public class RoutingTable
    extends ModelBase
{
    private Node rootNode;

    public Node getRootNode()
    {
        return rootNode;
    }

    public void setRootNode(Node rootNode)
    {
        this.rootNode = rootNode;
    }
}
