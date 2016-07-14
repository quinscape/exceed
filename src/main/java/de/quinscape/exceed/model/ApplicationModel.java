package de.quinscape.exceed.model;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.model.ModelNotFoundException;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private List<String> supportedLocales = Collections.singletonList("en_US");

    private List<String> styleSheets;

    private String schema;

    private Layout domainLayout;

    private String name;

    private ContextModel applicationContextModel;

    private ContextModel sessionContextModel;

    private StaticFunctionReferences staticFunctionReferences;


    public ApplicationModel()
    {
        domainLayout = new Layout();
        domainLayout.setName("domain");
    }


    /**
     * Database schema for this application
     * @return
     */
    public String getSchema()
    {
        return schema;
    }


    public void setSchema(String schema)
    {
        this.schema = schema;
    }


    /**
     * Routing table for this application
     * @return
     */
    @JSONProperty(ignore = true)
    public RoutingTable getRoutingTable()
    {
        return routingTable;
    }


    public void setRoutingTable(RoutingTable routingTable)
    {
        this.routingTable = routingTable;
    }


    /**
     * Map of domain types in the application. Actually defined in its own file.
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(DomainType.class)
    public Map<String, DomainType> getDomainTypes()
    {
        return domainTypesRO;
    }

    /**
     * Map of property types in the application. Actually defined in its own file.
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(PropertyType.class)
    public Map<String, PropertyType> getPropertyTypes()
    {
        return propertyTypesRO;
    }


    /**
     * Map of views in the application. Actually defined in its own file.
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(View.class)
    public Map<String, View> getViews()
    {
        return viewsRO;
    }


    /**
     * Stylesheets resource paths for this application
     *
     * @return
     */
    @JSONTypeHint(String.class)
    public List<String> getStyleSheets()
    {
        return styleSheets;
    }


    public void setStyleSheets(List<String> styleSheets)
    {
        this.styleSheets = styleSheets;
    }

    @Override
    @JSONProperty(ignore = true)
    public String getName()
    {
        return name;
    }


    /**
     * Merges the app.json located parts of the given application model with this application model.
     *
     * This is used internally to ensure location order independent parsing of application models. The system might
     * encounter the actual app.json file later than other parts it needs to merge into this. So the system creates
     * an empty application where it sets all the secondary (non-app.json) models, using this method to merge in the
     * contents of the actual app.json file.
     *
     * @param applicationModel application model
     */
    public void merge(ApplicationModel applicationModel)
    {
        this.styleSheets = applicationModel.styleSheets;
        this.schema = applicationModel.schema;
        this.applicationContextModel = applicationModel.applicationContextModel;
        this.sessionContextModel = applicationModel.sessionContextModel;
        this.supportedLocales = applicationModel.supportedLocales;
    }


    public void setDomainLayout(Layout domainLayout)
    {
        this.domainLayout = domainLayout;
    }


    public Layout getDomainLayout()
    {
        return domainLayout;
    }


    /**
     * Map of enum types in the application. Actually defined in its own file.
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(EnumType.class)
    public Map<String, EnumType> getEnums()
    {
        return enumsRO;
    }


    /**
     * Map of processes in the application. Actually defined in its own file.
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(Process.class)
    public Map<String, Process> getProcesses()
    {
        return processesRO;
    }

    public DomainType getDomainType(String name)
    {
        DomainType domainType = domainTypes.get(name);
        if (domainType == null)
        {
            throw new ModelNotFoundException("Cannot find domain type with name '" + name + "'");
        }

        return domainType;
    }

    public Process getProcess(String name)
    {
        Process process = processes.get(name);
        if (process == null)
        {
            throw new ModelNotFoundException("Cannot find process with name '" + name + "'");
        }

        return process;
    }

    public EnumType getEnum(String name)
    {
        EnumType enumType = enums.get(name);
        if (enumType == null)
        {
            throw new ModelNotFoundException("Cannot find enum with name '" + name + "'");
        }

        return enumType;
    }

    public View getView(String name)
    {
        View view = views.get(name);
        if (view == null)
        {
            throw new ModelNotFoundException("Cannot find view with name '" + name + "'");
        }

        return view;
    }

    public PropertyType getPropertyType(String name)
    {
        PropertyType propertyType = propertyTypes.get(name);
        if (propertyType == null)
        {
            throw new ModelNotFoundException("Cannot find propertyType with name '" + name + "'");
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


    @Override
    public void setName(String name)
    {
        this.name = name;
    }


    public ContextModel getApplicationContext()
    {
        return applicationContextModel;
    }


    public void setApplicationContext(ContextModel applicationContextModel)
    {
        this.applicationContextModel = applicationContextModel;
    }


    public ContextModel getSessionContext()
    {
        return sessionContextModel;
    }


    public void setSessionContext(ContextModel sessionContext)
    {
        this.sessionContextModel = sessionContext;
    }


    public void setStaticFunctionReferences(StaticFunctionReferences staticFunctionReferences)
    {
        this.staticFunctionReferences = staticFunctionReferences;
    }


    public StaticFunctionReferences getStaticFunctionReferences()
    {
        return staticFunctionReferences;
    }


    public List<String> getSupportedLocales()
    {
        if (supportedLocales == null)
        {
            return Collections.emptyList();
        }
        return supportedLocales;
    }


    public void setSupportedLocales(List<String> supportedLocales)
    {
        this.supportedLocales = supportedLocales;
    }
}
