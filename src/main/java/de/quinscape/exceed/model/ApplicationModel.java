package de.quinscape.exceed.model;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.View;
import org.svenson.JSONTypeHint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApplicationModel
{
    private RoutingTable routingTable;

    private Map<String, DomainType> domainTypes = new HashMap<>();

    private Map<String, PropertyType> propertyTypes = new HashMap<>();

    private Map<String, View> views = new HashMap<>();

    public ApplicationModel()
    {

    }

    public RoutingTable getRoutingTable()
    {
        return routingTable;
    }

    public void setRoutingTable(RoutingTable routingTable)
    {
        this.routingTable = routingTable;
    }

    public Map<String, DomainType> getDomainTypes()
    {
        return domainTypes;
    }


    @JSONTypeHint(DomainType.class)
    public void setDomainTypes(Map<String, DomainType> domainTypes)
    {
        this.domainTypes = domainTypes;
    }

    public Map<String, PropertyType> getPropertyTypes()
    {
        return propertyTypes;
    }

    @JSONTypeHint(PropertyType.class)
    public void setPropertyTypes(Map<String, PropertyType> propertyTypes)
    {
        this.propertyTypes = propertyTypes;
    }

    public Map<String, View> getViews()
    {
        return views;
    }

    @JSONTypeHint(View.class)
    public void setViews(Map<String, View> views)
    {
        this.views = views;
    }
}
