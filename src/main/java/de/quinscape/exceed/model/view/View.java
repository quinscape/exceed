package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.ModelBase;

public class View
    extends ModelBase
{
    private String id;

    private ElementNode root;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public ElementNode getRoot()
    {
        return root;
    }

    public void setRoot(ElementNode root)
    {
        this.root = root;
    }
}
