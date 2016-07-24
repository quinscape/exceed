package de.quinscape.exceed.runtime.component;

public class SelectOption
{
    private final String display;
    private final Object value;


    public SelectOption(String display, Object value)
    {
        this.display = display;
        this.value = value;
    }


    public Object getValue()
    {
        return value;
    }


    public String getDisplay()
    {
        return display;
    }
}

