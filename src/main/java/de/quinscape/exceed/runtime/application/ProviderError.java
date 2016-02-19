package de.quinscape.exceed.runtime.application;

public class ProviderError
{
    private final String text;


    private final int componentIndex;


    public ProviderError(String errorMessage, int componentIndex)
    {
        text = errorMessage;
        this.componentIndex = componentIndex;
    }


    public String getType()
    {
        return "error";
    }


    public String getText()
    {
        return text;
    }


    public int getComponentIndex()
    {
        return componentIndex;
    }


}
