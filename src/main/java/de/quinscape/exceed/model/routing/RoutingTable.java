package de.quinscape.exceed.model.routing;


import de.quinscape.exceed.model.Model;

public class RoutingTable
    extends Model
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
