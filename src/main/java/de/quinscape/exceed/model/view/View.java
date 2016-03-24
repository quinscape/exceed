package de.quinscape.exceed.model.view;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.AutoVersionedModel;
import org.svenson.JSONProperty;

import java.util.List;
import java.util.function.Predicate;

public class View
    extends TopLevelModel
    implements AutoVersionedModel
{
    private ComponentModel root;

    private List<String> comments;

    private boolean preview;

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


    public boolean isPreview()
    {
        return preview;
    }


    public void setPreview(boolean preview)
    {
        this.preview = preview;
    }


    /**
     * Returns the first component model in pre-order traversal
     * that matches the given predicate.
     *
     * @param predicate predicate
     *
     * @return first component model that matches the predicate
     */
    public ComponentModel find(Predicate<ComponentModel> predicate)
    {
        return root.find(predicate);
    }

}
