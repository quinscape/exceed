package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.Model;

public class View
    extends Model
{
    private String name;

    private ElementNode root;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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
