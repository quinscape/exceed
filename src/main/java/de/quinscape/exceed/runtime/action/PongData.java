package de.quinscape.exceed.runtime.action;

public class PongData
{
    private int value;


    public PongData()
    {
        this(0);
    }

    public PongData(int value)
    {
        this.value = value;
    }


    public int getValue()
    {
        return value;
    }


    public void setValue(int value)
    {
        this.value = value;
    }
}
