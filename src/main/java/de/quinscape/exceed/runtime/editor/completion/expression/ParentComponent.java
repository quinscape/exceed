package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.model.expression.Attributes;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

public class ParentComponent
{
    private final String componentName;

    private final Attributes attrs;


    public ParentComponent(
        @JSONParameter("componentName")
        String componentName,
        @JSONParameter("attrs")
        @JSONTypeHint(Attributes.class)
        Attributes attrs
    )
    {
        if (componentName == null)
        {
            throw new IllegalArgumentException("componentName can't be null");
        }

        this.componentName = componentName;
        this.attrs = attrs;
    }

    public String getComponentName()
    {
        return componentName;
    }


    public Attributes getAttrs()
    {
        return attrs;
    }
}

