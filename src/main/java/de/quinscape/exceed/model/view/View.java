package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.TopLevelModel;

public class View
    extends TopLevelModel
{
    private ComponentModel root;

    public ComponentModel getRoot()
    {
        return root;
    }

    public void setRoot(ComponentModel root)
    {
        this.root = root;
    }
}
