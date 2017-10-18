package de.quinscape.exceed.model.translation;

import de.quinscape.exceed.model.AbstractModel;

/**
 * Encapsulates a reference to a translation tag.
 */
public class ReferenceInfo
    extends AbstractModel
{
    private final ReferenceType referenceType;

    private final String name;


    public ReferenceInfo(ReferenceType referenceType, String name)
    {
        this.referenceType = referenceType;
        this.name = name;
    }


    public ReferenceType getReferenceType()
    {
        return referenceType;
    }


    public String getName()
    {
        return name;
    }
}
