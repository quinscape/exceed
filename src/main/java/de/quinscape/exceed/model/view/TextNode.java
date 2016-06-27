package de.quinscape.exceed.model.view;

public class TextNode
    extends ComponentModel
{
    public TextNode(String value)
    {
        Attributes attrs = new Attributes(null);
        attrs.setAttribute("value", value);
        this.setAttrs(attrs);
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

