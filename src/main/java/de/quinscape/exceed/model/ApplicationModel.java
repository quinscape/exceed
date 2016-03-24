package de.quinscape.exceed.model;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.model.ModelNotFoundException;
import org.svenson.JSONProperty;

import java.util.Collections;
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
    private Map<String, DomainType> domainTypesRO = Collections.unmodifiableMap(domainTypes);

    private Map<String, PropertyType> propertyTypes = new HashMap<>();
    private Map<String, PropertyType> propertyTypesRO = Collections.unmodifiableMap(propertyTypes);

    private Map<String, EnumType> enums = new HashMap<>();
    private Map<String, EnumType> enumsRO = Collections.unmodifiableMap(enums);

    private Map<String, View> views = new HashMap<>();
    private Map<String, View> viewsRO = Collections.unmodifiableMap(views);

    private Map<String, Process> processes = new HashMap<>();
    private Map<String, Process> processesRO = Collections.unmodifiableMap(processes);

    private AtomicLong idCount = new AtomicLong(0L);

    private List<String> styleSheets;

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
        return domainTypesRO;
    }

    @JSONProperty(ignore = true)
    public Map<String, PropertyType> getPropertyTypes()
    {
        return propertyTypesRO;
    }


    @JSONProperty(ignore = true)
    public Map<String, View> getViews()
    {
        return viewsRO;
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


    public Map<String, EnumType> getEnums()
    {
        return enumsRO;
    }


    public Map<String, Process> getProcesses()
    {
        return processesRO;
    }

    public DomainType getDomainType(String name)
    {
        DomainType domainType = domainTypes.get(name);
        if (domainType == null)
        {
            throw new ModelNotFoundException("Cannot for domain type with name" + name + "'");
        }

        return domainType;
    }

    public Process getProcess(String name)
    {
        Process process = processes.get(name);
        if (process == null)
        {
            throw new ModelNotFoundException("Cannot for process with name" + name + "'");
        }

        return process;
    }

    public EnumType getEnum(String name)
    {
        EnumType enumType = enums.get(name);
        if (enumType == null)
        {
            throw new ModelNotFoundException("Cannot for enum with name" + name + "'");
        }

        return enumType;
    }

    public View getView(String name)
    {
        View view = views.get(name);
        if (view == null)
        {
            throw new ModelNotFoundException("Cannot for view with name" + name + "'");
        }

        return view;
    }

    public PropertyType getPropertyType(String name)
    {
        PropertyType propertyType = propertyTypes.get(name);
        if (propertyType == null)
        {
            throw new ModelNotFoundException("Cannot for propertyType with name" + name + "'");
        }

        return propertyType;
    }


    public void addDomainType(String name, DomainType domainType)
    {
        domainTypes.put(name, domainType);
    }

    public void addEnum(String name, EnumType enumType)
    {
        enums.put(name, enumType);
    }

    public void addView(String name, View view)
    {
        views.put(name, view);
    }

    public void addProcess(String name, Process process)
    {
        process.setApplicationModel(this);
        processes.put(name, process);
    }

    public void addPropertyType(String name, PropertyType propertyType)
    {
        propertyTypes.put(name, propertyType);
    }

}
