package de.quinscape.exceed.runtime.editor.completion;

import de.quinscape.exceed.message.IncomingMessage;
import de.quinscape.exceed.message.MessageMeta;
import de.quinscape.exceed.message.Query;
import de.quinscape.exceed.runtime.editor.completion.expression.ParentComponent;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.List;

public class ComponentCompleteQuery
    extends Query
{
    private final List<ParentComponent> path;

    private final int index;


    public ComponentCompleteQuery(
        @JSONParameter("path")
        @JSONTypeHint(ParentComponent.class)
        List<ParentComponent> path,
        @JSONParameter("index")
        int index,
        @JSONParameter("propName")
        String propName,
        @JSONParameter("meta")
        MessageMeta meta
    )
    {
        super(meta);
        this.path = path;
        this.index = index;
    }


    public List<ParentComponent> getPath()
    {
        return path;
    }


    public int getIndex()
    {
        return index;
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
