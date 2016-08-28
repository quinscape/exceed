package de.quinscape.exceed.model.action;

public class SetActionModel
    extends ActionModel
{
    private String name;

    private String path;

    private Object value;

    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public Object getValue()
    {
        return value;
    }


    public void setValue(Object value)
    {
        this.value = value;
    }


    public String getPath()
    {
        return path;
    }


    public void setPath(String path)
    {
        this.path = path;
    }
}
