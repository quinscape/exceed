package de.quinscape.exceed.runtime.component.translation;

/**
 * Created by sven on 05.07.16.
 */
public class ReferenceInfo
{
    private final ReferenceType type;

    private final String name;


    public ReferenceInfo(ReferenceType type, String name)
    {
        this.type = type;
        this.name = name;
    }


    public ReferenceType getType()
    {
        return type;
    }


    public String getName()
    {
        return name;
    }
}
