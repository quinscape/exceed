package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.View;

public interface ModelService
{
    RoutingTable getRoutingTable();

    View findView(String name);

    DomainType findDomainType(String name);

    PropertyType findPropertyType(String name);
}
