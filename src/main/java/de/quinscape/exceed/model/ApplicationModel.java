package de.quinscape.exceed.model;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumModel;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.View;
import org.svenson.JSONProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Encapsulates the general application configuration.
 * <p>
 * The JSON-ignored properties contain models that live in their resource locations.
 *
 * @see de.quinscape.exceed.runtime.model.ModelCompositionService
 */
public class ApplicationModel
    extends TopLevelModel
{
    private RoutingTable routingTable;

    private Map<String, DomainType> domainTypes = new HashMap<>();


    private Map<String, PropertyType> propertyTypes = new HashMap<>();

    private Map<String, EnumModel> enums = new HashMap<>();

    private Map<String, View> views = new HashMap<>();

    private List<String> styleSheets;

    private AtomicLong idCount = new AtomicLong(0L);

    private String schema;

    private Layout domainLayout;


    public ApplicationModel()
    {
        domainLayout = new Layout();
        domainLayout.setName("domain");
    }


    public String getSchema()
    {
        return schema;
    }


    public void setSchema(String schema)
    {
        this.schema = schema;
    }


    @JSONProperty(ignore = true)
    public RoutingTable getRoutingTable()
    {
        return routingTable;
    }


    public void setRoutingTable(RoutingTable routingTable)
    {
        this.routingTable = routingTable;
    }


    @JSONProperty(ignore = true)
    public Map<String, DomainType> getDomainTypes()
    {
        return domainTypes;
    }


    public void setDomainTypes(Map<String, DomainType> domainTypes)
    {
        this.domainTypes = domainTypes;
    }


    @JSONProperty(ignore = true)
    public Map<String, PropertyType> getPropertyTypes()
    {
        return propertyTypes;
    }


    public void setPropertyTypes(Map<String, PropertyType> propertyTypes)
    {
        this.propertyTypes = propertyTypes;
    }


    @JSONProperty(ignore = true)
    public Map<String, View> getViews()
    {
        return views;
    }


    public void setViews(Map<String, View> views)
    {
        this.views = views;
    }


    public List<String> getStyleSheets()
    {
        return styleSheets;
    }


    public void setStyleSheets(List<String> styleSheets)
    {
        this.styleSheets = styleSheets;
    }


    @Override
    public String getName()
    {
        return "app.json";
    }


    public long nextId()
    {
        return idCount.incrementAndGet();
    }


    public void setIdCount(long idCount)
    {
        this.idCount.set(idCount);
    }


    public long getIdCount()
    {
        return idCount.get();
    }


    /**
     * copies the non-app.json data of the give application model into this one.
     *
     * @param applicationModel application model
     */
    public void merge(ApplicationModel applicationModel)
    {
        this.styleSheets = applicationModel.styleSheets;
        this.schema = applicationModel.schema;
    }


    public void setDomainLayout(Layout domainLayout)
    {
        this.domainLayout = domainLayout;
    }


    public Layout getDomainLayout()
    {
        return domainLayout;
    }


    public Map<String, EnumModel> getEnums()
    {
        return enums;
    }


    public void setEnums(Map<String, EnumModel> enums)
    {
        this.enums = enums;
    }
}
