package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.TopLevelModel;
import org.svenson.JSONProperty;

public class View
    extends TopLevelModel
{
    private ComponentModel root;

    /**
     *  cached JSON for the view itself. We need the view as view model, but we don't
     *  want to to keep converting it back to json for the client,
     */
    private String cachedJSON;

    public ComponentModel getRoot()
    {
        return root;
    }

    public void setRoot(ComponentModel root)
    {
        this.root = root;
    }

    public void setCachedJSON(String json)
    {
        this.cachedJSON = json;
    }

    @JSONProperty(ignore = true)
    public String getCachedJSON()
    {
        return cachedJSON;
    }
}
