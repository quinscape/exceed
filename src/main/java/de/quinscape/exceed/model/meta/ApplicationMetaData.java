package de.quinscape.exceed.model.meta;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.annotation.InjectResource;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.domain.StateMachine;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.domain.property.DecimalConverter;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.js.JsEnvironmentFactory;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.js.def.DefinitionsBuilder;
import de.quinscape.exceed.runtime.js.env.StateMachineTypeResolver;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;

import javax.script.CompiledScript;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates application meta data.
 * <p>
 * It contains all the things of the application model that are not primary artifacts (domain models, processes, views
 * etc) but secondary information about the application. Results of code analysis, graphical editor object layouts etc.
 * </p>
 */
public class ApplicationMetaData
{

    private final static Logger log = LoggerFactory.getLogger(ApplicationMetaData.class);

    private static final String DIALOG_COMPONENT = "Dialog";

    public static final List<ModelValidator> MODEL_VALIDATORS = Arrays.asList(
        new RoutingTableValidator(),
        new ExpressionTypeValidator()
    );

    private final ApplicationModel applicationModel;

    private final Definitions systemDefinitions;

    private StaticFunctionReferences staticFunctionReferences;

    private final ScopeMetaModel scopeMetaModel;

    private WebpackStats webpackStats;

    private CompiledScript serverJsBundle;

    private long serverJsTimestamp;

    private Map<PropertyTypeKey, PropertyType> propertyTypes;

    private int maxDecimalPlaces;

    private Definitions applicationDefinitions;

    private Set<ApplicationError> errors;

    private JsEnvironment jsEnvironment;

    public ApplicationMetaData(ApplicationModel applicationModel, Definitions systemDefinitions)
    {
        this.applicationModel = applicationModel;
        this.systemDefinitions = systemDefinitions;

        scopeMetaModel = new ScopeMetaModel(applicationModel, systemDefinitions);
    }


    public StaticFunctionReferences getStaticFunctionReferences()
    {
        return staticFunctionReferences;
    }


    @InjectResource("/resources/js/track-usage.json")
    public void setStaticFunctionReferences(StaticFunctionReferences staticFunctionReferences)
    {
        this.staticFunctionReferences = staticFunctionReferences;
    }



    public ScopeMetaModel getScopeMetaModel()
    {
        return scopeMetaModel;
    }


    @InjectResource("/resources/js/webpack-stats.json")
    public void setWebpackStats(WebpackStats webpackStats)
    {
        this.webpackStats = webpackStats;
    }


    public WebpackStats getWebpackStats()
    {
        return webpackStats;
    }


    @InjectResource("/resources/js/exceed-server.js")
    public void setServerJsBundle(CompiledScript serverJsBundle)
    {
        this.serverJsBundle = serverJsBundle;
        this.serverJsTimestamp = System.currentTimeMillis();
    }


    @JSONProperty(ignore = true)
    public long getServerJsTimestamp()
    {
        return serverJsTimestamp;
    }


    @JSONProperty(ignore = true)
    public CompiledScript getServerJsBundle()
    {
        return serverJsBundle;
    }


    public Map<PropertyTypeKey, PropertyType> getPropertyTypes()
    {
        return propertyTypes;
    }

    /**
     * Creates a new property type for the give property model or returns an already existing property type of
     * identical configuration.
     *
     * @see PropertyTypeKey
     *
     * @param type          property type name
     * @param typeParam     property type param
     * @param config        property type config
     *
     * @return property type
     */
    public PropertyType createPropertyType(String type, String typeParam, Map<String,Object> config)
    {
        final PropertyTypeModel propertyTypeModel = applicationModel.getPropertyType(type);

        PropertyTypeKey key = new PropertyTypeKey(
            type,
            typeParam,
            mergeConfig(propertyTypeModel.getDefaultConfig(), config)
        );

        if (propertyTypes == null)
        {
            propertyTypes = new HashMap<>();
        }
        
        final PropertyType cached = propertyTypes.get(key);
        if (cached != null)
        {
            log.debug("Reuse cached property type for " + key + ": " + cached);
            
            return cached;
        }


        final PropertyConverter<?, ?, ?> converter = propertyTypeModel.createConverter(
            applicationModel,
            key.getTypeParam(),
            key.getConfig()
        );

        final PropertyType propertyType = new PropertyType( key, propertyTypeModel, converter);
        propertyTypes.put(key, propertyType);
        return propertyType;
    }


    private Map<String, Object> mergeConfig(Map<String, Object> defaultConfig, Map<String, Object> config)
    {
        Map<String, Object> map;
        if (config == null || config.size() == 0)
        {
            map = defaultConfig;
        }
        else if (defaultConfig == null || defaultConfig.size() == 0)
        {
            map = config;
        }
        else
        {
            map = new HashMap<>(defaultConfig);
            map.putAll(config);
        }
        return map != null ? map : Collections.emptyMap();
    }


    public PropertyType createPropertyType(PropertyModel propertyModel)
    {
        return createPropertyType(
            propertyModel.getType(),
            propertyModel.getTypeParam(),
            propertyModel.getConfig()
        );
    }
    public void createPropertyTypes(ContextModel contextModel)
    {
        if (contextModel == null)
        {
            return;
        }

        for (ScopedPropertyModel propertyModel : contextModel.getProperties().values())
        {
            PropertyType.get(applicationModel, propertyModel);
        }
    }


    public void postProcess()
    {
        createPropertyTypes(applicationModel.getConfigModel().getApplicationContextModel());
        createPropertyTypes(applicationModel.getConfigModel().getSessionContextModel());

        for (Process process : applicationModel.getProcesses().values())
        {
            createPropertyTypes(process.getContextModel());
        }

        for (View view : applicationModel.getViews().values())
        {
            createDialogStateProperties(view);

            createPropertyTypes(view.getContextModel());
        }

        maxDecimalPlaces = getMaxDecimalPlaces(applicationModel);

        final DefinitionsBuilder builder = Definition.builder();

        builder.merge(systemDefinitions);
        registerRuleFunctions(builder);
        registerEnumIdentifiers(builder);
        registerStateMachineIdentifiers(builder);

        applicationDefinitions = builder.build();

        scopeMetaModel.init(applicationDefinitions);

        checkContextDomainConflicts();

    }


    private void checkContextDomainConflicts()
    {
        final Set<String> domainTypeNames = applicationModel.getDomainTypes().keySet();

        for (ScopeDeclarations scopeDeclarations : scopeMetaModel.getAllDeclarations())
        {
            for (String decl : scopeDeclarations.getDeclarations().keySet())
            {
                if (domainTypeNames.contains(decl))
                {
                    throw new InconsistentModelException("Scope declaration " + decl + " in scope location '" + scopeDeclarations.getScopeLocation() + "' has the same name as a domain type." +
                        "Since we embed context expressions in query() expressions, this would cause a conflict. It is best to stick to uppercase names for Domain Types and lower case names for context identifiers"
                    );
                }
            }
        }
    }

    private void createDialogStateProperties(View view)
    {
        ContextModel contextModel = view.getContextModel();
        if (contextModel == null)
        {
            contextModel = new ContextModel();
            view.setContextModel(contextModel);
        }

        final Map<String, ScopedPropertyModel> properties;
        if (contextModel.getProperties() == null)
        {
            properties = new HashMap<>();
        }
        else
        {
            properties = new HashMap<>(contextModel.getProperties());
        }


        final List<ComponentModel> dialogs = ComponentUtil.findComponents(
            view,
            c -> c.getName().equals(DIALOG_COMPONENT)
        );

        for (ComponentModel dialog : dialogs)
        {
            final String name = dialog.getComponentId();

            if (name == null)
            {
                throw new InconsistentModelException("Dialogs must have an id attribute: " + dialog);
            }

            final ScopedPropertyModel dialogStateProp = DomainProperty.builder()
                .withName(name)
                .withType("Enum", "DialogState")
                .withDefaultValue("DialogState.CLOSED")
                .buildScoped();
            
            final ScopedPropertyModel existingProp = properties.put(name, dialogStateProp);

            if (existingProp != null)
            {
                throw new InconsistentModelException("Naming conflict between " + dialogStateProp + " and " + existingProp);
            }

            contextModel.setProperties(properties);
        }
    }


    private void registerEnumIdentifiers(DefinitionsBuilder builder)
    {
        for (EnumType enumType : applicationModel.getEnums().values())
        {
            builder.identifier(enumType.getName())
                .withDescription(enumType.getDescription())
                .withPropertyType(PropertyType.MAP, enumType.getName())
                .withType(DefinitionType.ENUM)
                .build();
        }

    }

    private void registerStateMachineIdentifiers(DefinitionsBuilder builder)
    {
        for (StateMachine stateMachine : applicationModel.getStateMachines().values())
        {
            builder.identifier(stateMachine.getName())
                .withDescription(stateMachine.getDescription())
                .withPropertyTypeResolver(new StateMachineTypeResolver(stateMachine))
                .withType(DefinitionType.STATE_MACHINE)
                .build();
        }

    }


    public void validate()
    {
        ModelValidationContext ctx = new ModelValidationContext();

        for (ModelValidator validator : MODEL_VALIDATORS)
        {
            validator.validate(ctx, applicationModel);
        }

        errors = ctx.getErrors();
    }

    public void initJsEnv(JsEnvironmentFactory factory)
    {
        if (this.jsEnvironment == null)
        {
            this.jsEnvironment = factory.create(applicationModel);
        }
    }


    private void registerRuleFunctions(DefinitionsBuilder builder)
    {
        for (DomainRule domainRule : applicationModel.getDomainRules().values())
        {
            final DomainProperty target = domainRule.getTarget();
            builder.function(domainRule.getName())
                .withDescription(domainRule.getDescription())
                .withParameterModels(target)
                .withReturnType(ExpressionUtil.BOOLEAN_TYPE)
                .withType(DefinitionType.RULE)
                .build();
        }
    }


    private int getMaxDecimalPlaces(ApplicationModel applicationModel)
    {
        int max = -1;

        max = Math.max(max, getMaxDecimalPlaces(applicationModel.getApplicationContextModel()));
        max = Math.max(max, getMaxDecimalPlaces(applicationModel.getSessionContextModel()));


        for (DomainType domainType : applicationModel.getDomainTypes().values())
        {
            max = Math.max(max, getMaxDecimalPlaces(domainType));
        }

        for (Process process : applicationModel.getProcesses().values())
        {
            max = Math.max(max, getMaxDecimalPlaces(process.getContextModel()));
        }

        for (View view : applicationModel.getViews().values())
        {
            max = Math.max(max, getMaxDecimalPlaces(view.getContextModel()));
        }
        return max;
    }


    private int getMaxDecimalPlaces(DomainType domainType)
    {
        final int defaultDecimalPlaces = applicationModel.getConfigModel().getDecimalConfig().getDefaultDecimalPlaces();

        int maxDecimalPlaces = -1;
        for (DomainProperty propertyModel : domainType.getProperties())
        {
            if (propertyModel.getType().equals(PropertyType.DECIMAL))
            {

                final int decimalPlaces = DecimalConverter.getDecimalPlaces(defaultDecimalPlaces, propertyModel
                    .getConfig());
                maxDecimalPlaces = Math.max(maxDecimalPlaces, decimalPlaces);
            }
        }

        return maxDecimalPlaces;
    }


    private int getMaxDecimalPlaces(ContextModel contextModel)
    {
        final int defaultDecimalPlaces = applicationModel.getConfigModel().getDecimalConfig().getDefaultDecimalPlaces();

        int maxDecimalPlaces = -1;
        if (contextModel != null)
        {
            for (ScopedPropertyModel propertyModel : contextModel.getProperties().values())
            {
                if (propertyModel.getType().equals(PropertyType.DECIMAL))
                {
                    final int decimalPlaces = DecimalConverter.getDecimalPlaces(defaultDecimalPlaces, propertyModel.getConfig());

                    maxDecimalPlaces = Math.max(maxDecimalPlaces, decimalPlaces);
                }
            }
        }
        return maxDecimalPlaces;
    }


    /**
     * Returns the largest number of decimal places used in properties within the application or <code>-1</code> if no
     * Decimal properties were found.
     *
     * @return  max decimal places or <code>-1</code>
     */
    public int getMaxDecimalPlaces()
    {
        return maxDecimalPlaces;
    }


    public Set<ApplicationError> getErrors()
    {
        return errors;
    }


    /**
     * Returns the application definitions
     *
     * @see ScopeDeclarations#getLocalDefinitions()
     */
    public Definitions getApplicationDefinitions()
    {
        return applicationDefinitions;
    }


    public JsEnvironment getJsEnvironment()
    {
        return jsEnvironment;
    }


    public Definitions getSystemDefinitions()
    {
        return systemDefinitions;
    }
}
