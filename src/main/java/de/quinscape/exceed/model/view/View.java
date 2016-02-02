package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.VersionedModel;
import org.svenson.JSONProperty;

import java.util.List;

public class View
    extends TopLevelModel
    implements VersionedModel
{
    private ComponentModel root;

    private List<String> comments;

    /**
     *  cached JSON for the view itself. We need the view as view model, but we don't
     *  want to to keep converting it back to json for the client,
     */
    private String cachedJSON;

    private String version;


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


    public List<String> getComments()
    {
        return comments;
    }


    public void setComments(List<String> comments)
    {
        this.comments = comments;
    }


    @Override
    public String getVersion()
    {

        return version;
    }


    @Override
    public void setVersion(String version)
    {
        this.version = version;
    }
}
