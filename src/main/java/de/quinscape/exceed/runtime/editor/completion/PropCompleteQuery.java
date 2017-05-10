package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.util.ComponentPath;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

public class PropCompleteQuery
    extends PropNameCompleteQuery
{
    private final String propName;


    public PropCompleteQuery(

        @JSONParameter("path")
        ComponentPath path,

        @JSONParameter("viewModel")
        @JSONTypeHint(View.class)
        View viewModel,

        @JSONParameter("propName")
        String propName,

        @JSONParameter("meta")
        MessageMeta meta
    )
    {
        super(path, viewModel, meta);

        this.propName = propName;
    }


    public String getPropName()
    {
        return propName;
    }
}
