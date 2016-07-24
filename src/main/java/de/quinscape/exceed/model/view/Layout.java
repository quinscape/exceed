package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModel;

/**
 * Reusable top level layout definition model. The default layout for the application is defined in the application model.
 *
 * Views can override the default by providing a layout prop to their root View component.
 *
 * @see ApplicationModel#getDefaultLayout()
 */
public class Layout
    extends TopLevelModel
{
    /**
     * Name of the component marking the place to insert the individual view's components.
     */
    public static final String CONTENT = "Content";

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
