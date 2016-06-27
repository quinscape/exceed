package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.message.Query;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.editor.completion.expression.ParentComponent;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.List;

public class ComponentCompleteQuery
    extends Query
{
    private final List<ParentComponent> path;

    private final int index;

    private final View viewModel;


    public ComponentCompleteQuery(
        @JSONParameter("meta")
        MessageMeta meta,

        @JSONParameter("path")
        @JSONTypeHint(ParentComponent.class)
        List<ParentComponent> path,

        @JSONParameter("index")
        int index,

        @JSONParameter("viewModel")
        @JSONTypeHint(View.class)
        View viewModel

        )
    {
        super(meta);
        this.path = path;
        this.index = index;
        this.viewModel = viewModel;
    }


    public List<ParentComponent> getPath()
    {
        return path;
    }


    public int getIndex()
    {
        return index;
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
            + ", index = " + index
            ;
    }
}
