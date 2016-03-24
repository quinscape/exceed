package de.quinscape.exceed.runtime.application;

public class ComponentError
{
    private final String text;

    private final String value;

    private final int componentIndex;

    private final String attrName;


    public ComponentError(String value, Throwable expressionError, int componentIndex, String attrName)
    {
        text = expressionError.getMessage();
        this.value = value;
        this.componentIndex = componentIndex;
        this.attrName = attrName;
    }


    public String getType()
    {
        return "error";
    }


    public String getText()
    {
        return text;
    }


    public String getValue()
    {
        return value;
    }


    public int getComponentIndex()
    {
        return componentIndex;
    }


    public String getAttrName()
    {
        return attrName;
    }
}
