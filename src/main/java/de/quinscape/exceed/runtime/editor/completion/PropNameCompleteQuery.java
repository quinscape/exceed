package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.message.Query;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.util.ComponentPath;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

public class PropNameCompleteQuery
    extends Query
{

    private final View viewModel;

    private final ComponentPath path;

    public PropNameCompleteQuery(
        @JSONParameter("path")
        ComponentPath path,
        @JSONParameter("viewModel")
        @JSONTypeHint(View.class)
        View viewModel,
        @JSONParameter("meta")
        MessageMeta meta
    )
    {
        super(meta);

        this.path = path;
        this.viewModel = viewModel;
    }


    public ComponentPath getPath()
    {
        return path;
    }


    public View getViewModel()
    {
        return viewModel;
    }



    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "path = " + path
            + ", viewModel = " + viewModel
            ;
    }

}
