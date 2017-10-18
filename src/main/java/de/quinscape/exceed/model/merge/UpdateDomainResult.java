package de.quinscape.exceed.model.merge;

import de.quinscape.exceed.model.domain.type.DomainType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the result of a update domain action. This is a map of the new version UUIDs for the types involved
 * and a list of model merge conflicts if those exist.
 * <p>
 * Result of a model editing storage operation that we don't want to execute as-is since someone concurrently
 * modified the models changed in the model editing storage operation.
 * <p>
 * The conflict can be either a differetent versionGUID for the same identityGUID or a naming conflict of new objects.
 * </p>
 */
public class UpdateDomainResult
{
    private final Map<String, DomainType> mergedDomainTypes;

    private final List<MergeLocation> locations;


    private UpdateDomainResult(Map<String, DomainType> mergedDomainTypes, List<MergeLocation> locations)
    {
        this.mergedDomainTypes = mergedDomainTypes;
        this.locations = locations;
    }


    public Map<String, DomainType> getMergedDomainTypes()
    {
        return mergedDomainTypes;
    }


    public boolean isOk()
    {
        return locations.size() == 0;
    }


    public List<MergeLocation> getLocations()
    {
        return locations;
    }


    public static UpdateDomainResult createMergeError(List<MergeLocation> locations)
    {
        return new UpdateDomainResult(Collections.emptyMap(), locations);
    }

    public static UpdateDomainResult createSuccess(Map<String, DomainType> domainTypes)
    {
        return new UpdateDomainResult(domainTypes, Collections.emptyList());
    }
}
