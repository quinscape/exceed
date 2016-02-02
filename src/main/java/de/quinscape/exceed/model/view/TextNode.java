package de.quinscape.exceed.model.view;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import de.quinscape.exceed.runtime.util.Util;
import org.svenson.JSON;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;
import org.svenson.StringBuilderSink;

import java.util.Collections;
import java.util.List;

public class TextNode
    extends ComponentModel
{
    public TextNode(String value)
    {
        try
        {
            Attributes attrs = new Attributes(null);
            attrs.setAttribute("value", value);
            this.setAttrs(attrs);
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException("Error creating string model", e);
        }
    }


    @Override
    public void setName(String name)
    {
        if (!STRING_MODEL_NAME.equals(name))
        {
            throw new IllegalArgumentException("Invalid text node name" + name);
        }
    }


    @Override
    public String getName()
    {
        return STRING_MODEL_NAME;
    }
}

