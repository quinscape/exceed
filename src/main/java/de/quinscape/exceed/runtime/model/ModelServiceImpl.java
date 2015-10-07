package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.View;

import java.util.concurrent.ConcurrentMap;

public class ModelServiceImpl
    implements ModelService
{
    private ConcurrentMap<String, ModelHandle> handles;

    @Override
    public RoutingTable getRoutingTable()
    {
        return null;
    }

    @Override
    public View findView(String name)
    {
        return null;
    }

    @Override
    public DomainType findDomainType(String name)
    {
        return null;
    }

    @Override
    public PropertyType findPropertyType(String name)
    {
        return null;
    }

    private static class ModelHandle
    {
    }
}
