package de.quinscape.exceed.model;

import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.StateMachine;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.meta.ApplicationMetaData;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.domain.DomainTypesRegistry;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ModelNotFoundException;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Encapsulates the general application configuration. It corresponds to no model JSON file on its own, but is an
 * aggregation of all model JSON resources.
 *
 * @see de.quinscape.exceed.runtime.model.ModelCompositionService
 */
public class ApplicationModel
    extends AbstractModel
    implements DomainTypesRegistry
{
    private RoutingTable routingTable;

    private Map<String, DomainType> domainTypes = new HashMap<>();

    private Map<String, DomainType> domainTypesRO = Collections.unmodifiableMap(domainTypes);

    private Map<String, DomainVersion> domainVersions = new HashMap<>();

    private Map<String, DomainVersion> domainVersionsRO = Collections.unmodifiableMap(domainVersions);

    private Map<String, PropertyTypeModel> propertyTypes = new HashMap<>();

    private Map<String, PropertyTypeModel> propertyTypesRO = Collections.unmodifiableMap(propertyTypes);

    private Map<String, EnumType> enums = new HashMap<>();

    private Map<String, EnumType> enumsRO = Collections.unmodifiableMap(enums);

    private Map<String, View> views = new HashMap<>();

    private Map<String, View> viewsRO = Collections.unmodifiableMap(views);

    private Map<String, Process> processes = new HashMap<>();

    private Map<String, Process> processesRO = Collections.unmodifiableMap(processes);

    private Map<String, LayoutModel> layouts = new HashMap<>();

    private Map<String, LayoutModel> layoutsRO = Collections.unmodifiableMap(layouts);

    private Map<String, DomainRule> domainRules = new HashMap<>();

    private Map<String, DomainRule> domainRulesRO = Collections.unmodifiableMap(domainRules);

    private Map<String, StateMachine> stateMachines = new HashMap<>();

    private Map<String, StateMachine> stateMachinesRO = Collections.unmodifiableMap(stateMachines);

    private String name;

    private ApplicationConfig configModel = new ApplicationConfig();

    private ApplicationMetaData metaData;

    private String version;

    private Map<String, QueryTypeModel> queryTypes = new HashMap<>();

    private Map<String, QueryTypeModel> queryTypesRO = Collections.unmodifiableMap(queryTypes);

    public ApplicationModel()
    {
        this(null);
    }


    public ApplicationModel(Definitions definitions)
    {
        metaData = new ApplicationMetaData(this, definitions);
        version = UUID.randomUUID().toString();

    }


    /**
     * Routing table for this application
     *
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
     * Map of domain types in the application. Actually defined in their own file.
     *
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(DomainTypeModel.class)
    public Map<String, DomainType> getDomainTypes()
    {
        return domainTypesRO;
    }


    public Map<String, DomainVersion> getDomainVersions()
    {
        return domainVersionsRO;
    }


    /**
     * Map of property types in the application. Actually defined in their own file.
     *
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(PropertyTypeModel.class)
    public Map<String, PropertyTypeModel> getPropertyTypes()
    {
        return propertyTypesRO;
    }


    /**
     * Map of views in the application. Actually defined in their own file.
     *
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(View.class)
    public Map<String, View> getViews()
    {
        return viewsRO;
    }


    @JSONProperty(ignore = true)
    public String getName()
    {
        return name;
    }


    /**
     * Map of enum types in the application. Actually defined in their own file.
     *
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(EnumType.class)
    public Map<String, EnumType> getEnums()
    {
        return enumsRO;
    }


    /**
     * Map of processes in the application. Actually defined in their own file.
     *
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(Process.class)
    public Map<String, Process> getProcesses()
    {
        return processesRO;
    }


    /**
     * Map of layout in the application. Actually defined in their own file.
     *
     * @return
     */
    @JSONProperty(ignore = true)
    @JSONTypeHint(LayoutModel.class)
    public Map<String, LayoutModel> getLayouts()
    {
        return layoutsRO;
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


    public LayoutModel getLayout(String name)
    {
        final LayoutModel layout = layouts.get(name);
        if (layout == null)
        {
            throw new ModelNotFoundException("Cannot find layout with name '" + name + "'");
        }

        return layout;
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


    public PropertyTypeModel getPropertyType(String name)
    {
        PropertyTypeModel propertyType = propertyTypes.get(name);
        if (propertyType == null)
        {
            throw new ModelNotFoundException("Cannot find propertyType with name '" + name + "'");
        }

        return propertyType;
    }


    public void addDomainType(DomainType domainType)
    {
        domainTypes.put(domainType.getName(), domainType);
    }


    public void addDomainVersion(DomainVersion domainVersion)
    {
        domainVersions.put(domainVersion.getName(), domainVersion);
    }


    public void addEnum(EnumType enumType)
    {
        enums.put(enumType.getName(), enumType);
    }


    public void addView(View view)
    {
        views.put(view.getName(), view);
    }


    public void addProcess(Process process)
    {
        processes.put(process.getName(), process);
    }

    public void addLayout(LayoutModel layout)
    {
        layouts.put(layout.getName(), layout);
    }


    public void addPropertyType(PropertyTypeModel propertyType)
    {
        propertyTypes.put(propertyType.getName(), propertyType);
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public ApplicationMetaData getMetaData()
    {
        return metaData;
    }


    public void setConfigModel(ApplicationConfig configModel)
    {
        this.configModel = configModel;
    }


    public ApplicationConfig getConfigModel()
    {
        return configModel;
    }
    
    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }


    public void addDomainRule(DomainRule domainRule)
    {
        this.domainRules.put(domainRule.getName(), domainRule);
    }

    public void addQueryType(QueryTypeModel queryTypeModel)
    {
        this.queryTypes.put(queryTypeModel.getName(), queryTypeModel);

    }

    public Map<String, DomainRule> getDomainRules()
    {
        return domainRulesRO;
    }


    @JSONProperty(ignore = true)
    @JSONTypeHint(LayoutModel.class)
    public Map<String, QueryTypeModel> getQueryTypes()
    {
        return queryTypesRO;
    }


    /**
     * Returns the valid scope declarations for the given process state
     *
     * @param processState      process state
     * @return
     */
    public ScopeDeclarations lookup(ProcessState processState)
    {
        return lookup(ScopeMetaModel.getScopeKey(processState));
    }


    /**
     * Returns the valid scope declarations for the non-process view.
     *
     * @param view
     * @return
     */
    public ScopeDeclarations lookup(View view)
    {
        return lookup(ScopeMetaModel.getScopeKey(view));
    }



    /**
     * Returns the valid scope declarations for the given string key.
     *
     * @param scopeKey  location identifier
     * @return
     */
    public ScopeDeclarations lookup(String scopeKey)
    {
        final ScopeMetaModel scopeMetaModel = getMetaData().getScopeMetaModel();
        if (scopeMetaModel == null)
        {
            return null;
        }
        return scopeMetaModel.lookup(scopeKey);
    }


    public void addStateMachine(StateMachine stateMachine)
    {
        stateMachines.put(stateMachine.getName(), stateMachine);
    }

    public Map<String, StateMachine> getStateMachines()
    {
        return stateMachinesRO;
    }
}
