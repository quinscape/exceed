package de.quinscape.exceed.model.merge;

import de.quinscape.exceed.model.TopLevelModel;

/**
 * Encapsulates a conflict between two versions of the same model, either due to model identity (identityGUID match) or
 * a naming conflict.
 */
public class MergeLocation
{
    private final TopLevelModel ours;

    private final TopLevelModel theirs;

    public MergeLocation(TopLevelModel ours, TopLevelModel theirs)
    {
        this.ours = ours;
        this.theirs = theirs;
    }


    public TopLevelModel getOurs()
    {
        return ours;
    }


    public TopLevelModel getTheirs()
    {
        return theirs;
    }
}

