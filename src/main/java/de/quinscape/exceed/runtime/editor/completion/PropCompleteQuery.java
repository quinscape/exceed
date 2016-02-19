package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.model.view.View;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.List;

public class PropCompleteQuery
    extends PropNameCompleteQuery
{
    private final String propName;


    public PropCompleteQuery(
        @JSONParameter("path")
        List<Long> path,
        @JSONParameter("viewModel")
        @JSONTypeHint(View.class)
        View viewModel,
        @JSONParameter("propName")
        String propName,
        @JSONParameter("meta")
        MessageMeta
            meta)
    {
        super(path, viewModel, meta);

        this.propName = propName;
    }


    public String getPropName()
    {
        return propName;
    }
}
