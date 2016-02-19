package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.message.IncomingMessage;
import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.message.Query;
import de.quinscape.exceed.model.view.View;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.List;

public class PropNameCompleteQuery
    extends Query
{
    private final List<Long> path;

    private final View viewModel;


    public PropNameCompleteQuery(
        @JSONParameter("path")
        List<Long> path,
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


    public List<Long> getPath()
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
