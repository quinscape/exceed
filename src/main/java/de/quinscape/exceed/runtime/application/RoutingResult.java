package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.routing.Mapping;

/**
 * Encapsulates the result of the routing table resolution process.
 */
public class RoutingResult
{
    private final Mapping mapping;

    public RoutingResult(Mapping mapping)
    {
        this.mapping = mapping;
    }

    public Mapping getMapping()
    {
        return mapping;
    }
}
