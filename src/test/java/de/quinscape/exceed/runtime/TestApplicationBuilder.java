package de.quinscape.exceed.runtime;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.domain.StateMachine;
import de.quinscape.exceed.model.view.ComponentModelBuilder;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.action.DefaultActionService;
import de.quinscape.exceed.runtime.action.builtin.ExceedBaseActions;
import de.quinscape.exceed.runtime.action.param.ContextPropertyValueProviderFactory;
import de.quinscape.exceed.runtime.action.param.RuntimeContextProviderFactory;
import de.quinscape.exceed.runtime.config.ExpressionConfiguration;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.DomainServiceImpl;
import de.quinscape.exceed.runtime.js.DefaultExpressionCompiler;
import de.quinscape.exceed.runtime.js.JsEnvironmentFactory;
import de.quinscape.exceed.runtime.js.JsExpressionRenderer;
import de.quinscape.exceed.runtime.js.TypeAnalyzer;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ClientViewJSONGenerator;
import de.quinscape.exceed.runtime.model.ModelCompositionService;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import de.quinscape.exceed.runtime.model.ModelLocationRules;
import de.quinscape.exceed.runtime.model.TestRegistry;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.service.model.ModelSchemaService;
import de.quinscape.exceed.runtime.util.JsUtil;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;
import org.svenson.tokenize.InputStreamSource;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestApplicationBuilder
{
    private final static Logger log = LoggerFactory.getLogger(TestApplicationBuilder.class);

    private Map<String, DomainType> domainTypes = new HashMap<>();

    private String name = "TestApp";

    private Map<String, EnumType> enums = new HashMap<>();

    private boolean registerBaseProperties = true;

    private Map<String, PropertyTypeModel> propertyTypes = new HashMap<>();

    private Map<String, Process> processes = new HashMap<>();

    private Map<String, DomainRule> domainRules = new HashMap<>();

    private Map<String, StateMachine> stateMachines = new HashMap<>();

    private DomainService domainService;

    private static ModelSchemaService modelSchemaService = create();

    private Map<String, View> views = new HashMap<>();

    private ContextModel applicationContext;

    private ContextModel sessionContext;


    private Definitions systemDefinitions;

    private List<ResourceRoot> extensions;

    private Object testActionBean;


    public TestApplicationBuilder()
    {
        log.trace("Create test app builder");
    }


    private static ModelSchemaService create()
    {
        try
        {
            modelSchemaService = new ModelSchemaService();
            modelSchemaService.init();
            return modelSchemaService;
        }
        catch (ClassNotFoundException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    public TestApplicationBuilder withDomainType(String name, DomainType domainType)
    {
        domainTypes.put(name, domainType);
        return this;
    }

    public TestApplicationBuilder withView(View view)
    {
        views.put(view.getName(), view);
        return this;
    }


    public TestApplicationBuilder withEnum(String name, EnumType enumType)
    {
        enums.put(name, enumType);
        return this;
    }


    public TestApplicationBuilder withName(String name)
    {
        this.name = name;
        return this;
    }


    public TestApplicationBuilder withDefinitions(Definitions systemDefinitions)
    {
        this.systemDefinitions = systemDefinitions;
        return this;
    }

    public TestApplicationBuilder withApplicationContext(ScopedPropertyModel property)
    {
        final Map<String, ScopedPropertyModel> props;
        if (applicationContext == null)
        {
            applicationContext = new ContextModel();
            props = new HashMap<>();
            applicationContext.setProperties(props);
        }
        else
        {
            props = applicationContext.getProperties();
        }

        props.put(property.getName(), property);

        return this;
    }

    public TestApplicationBuilder withSessionContext(ScopedPropertyModel property)
    {
        final Map<String, ScopedPropertyModel> props;
        if (sessionContext == null)
        {
            sessionContext = new ContextModel();
            props = new HashMap<>();
            sessionContext.setProperties(props);
        }
        else
        {
            props = sessionContext.getProperties();
        }

        props.put(property.getName(), property);

        return this;
    }


    public TestApplicationBuilder withPropertyType(PropertyTypeModel propertyType)
    {
        propertyTypes.put(propertyType.getName(), propertyType);
        return this;
    }


    public TestApplicationBuilder withBaseProperties(boolean registerBaseProperties)
    {
        this.registerBaseProperties = registerBaseProperties;

        return this;
    }

    public TestApplicationBuilder withDomainService(DomainService domainService)
    {
        this.domainService = domainService;
        return this;
    }

    public TestApplicationBuilder withExtensions(File... dirs)
    {
        try
        {
            List<ResourceRoot> extensions = new ArrayList<>(dirs.length);
            for (File dir : dirs)
            {
                extensions.add(new FileResourceRoot(dir, false));
            }

            this.extensions = extensions;
            return this;
        }
        catch (IOException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    public TestApplicationBuilder withTestActionBean(Object testActionBean)
    {
        this.testActionBean = testActionBean;
        return this;
    }

    public TestApplication build()
    {
        ActionService actionService = new DefaultActionService(
            Arrays.asList(
                new ExceedBaseActions(null, null),
                testActionBean
            ),
            Arrays.asList(
                new RuntimeContextProviderFactory(),
                new ContextPropertyValueProviderFactory()
            )
        );

        Definitions baseDefinitions = Definition.builder()
            .merge(systemDefinitions)
            .merge(new ExpressionConfiguration().functionDefinitions(actionService))
            .build();

        final ModelJSONServiceImpl modelJSONService = new ModelJSONServiceImpl();
        modelJSONService.setClientViewJSONGenerator(new ClientViewJSONGenerator(actionService));

        final NashornScriptEngine nashorn = JsUtil.createEngine();

        ApplicationModel applicationModel;
        final ModelCompositionService svc = new ModelCompositionService(
            new ModelLocationRules(),
            modelSchemaService,
            new TestRegistry(Collections.emptyMap()),
            modelJSONService,
            new JsEnvironmentFactory(
                actionService,
                nashorn,
                new DefaultExpressionCompiler(
                    nashorn,
                    new JsExpressionRenderer(
                        Collections.emptyList()
                    ),
                    new TypeAnalyzer()
                )
            )
        );

        if (extensions != null)
        {
            final ResourceLoader resourceLoader = new ResourceLoader(extensions);
            applicationModel = svc.compose(resourceLoader.getAllResources().values(), domainService, baseDefinitions, name);
        }
        else
        {
            applicationModel = new ApplicationModel(baseDefinitions);
            applicationModel.setName(name);
        }

        for (DomainType domainType : domainTypes.values())
        {
            applicationModel.addDomainType(domainType);
        }

        for (DomainRule domainRule : domainRules.values())
        {
            applicationModel.addDomainRule(domainRule);
        }

        for (EnumType enumType : enums.values())
        {
            applicationModel.addEnum(enumType);
        }

        for (StateMachine stateMachine : stateMachines.values())
        {
            applicationModel.addStateMachine(stateMachine);
        }

        if (registerBaseProperties)
        {
            propertyTypes.putAll(readBaseProperties());
        }

        if (!applicationModel.getDomainTypes().containsKey("AppTranslation"))
        {
            readEssentialDomainTypeDefs(applicationModel);
        }

        for (PropertyTypeModel propertyType : propertyTypes.values())
        {
            applicationModel.addPropertyType(propertyType);
        }

        for (View view : views.values())
        {
            applicationModel.addView(view);
        }

        final LayoutModel defaultLayout = new LayoutModel();
        defaultLayout.setName(applicationModel.getConfigModel().getDefaultLayout());
        defaultLayout.setRoot(ComponentModelBuilder.component("div").getComponent());
        applicationModel.addLayout(defaultLayout);

        if (domainService == null)
        {
            domainService = new DomainServiceImpl(new TestStorageConfigurationRepository());

        }
        else
        {
            final Map<String, DomainType> domainTypes = domainService.getDomainTypes();
            for (DomainType domainType : domainTypes.values())
            {
                if (!applicationModel.getEnums().containsKey(domainType.getName()))
                {
                    applicationModel.addDomainType(domainType);
                }
            }

            for (DomainType domainType : applicationModel.getDomainTypes().values())
            {
                if (!domainTypes.containsKey(domainType.getName()))
                {
                    domainTypes.put(domainType.getName(), domainType);
                }
            }

            for (EnumType enumType : domainService.getEnums().values())
            {
                if (!applicationModel.getEnums().containsKey(enumType.getName()))
                {
                    applicationModel.addEnum(enumType);
                }
            };

        }


        if (applicationContext != null)
        {
            applicationModel.getConfigModel().setApplicationContextModel(applicationContext);
        }
        if (sessionContext != null)
        {
            applicationModel.getConfigModel().setSessionContextModel(sessionContext);
        }
        readServerBundle(applicationModel, nashorn);

        svc.postprocess(applicationModel);

        final TestApplication testApplication = new TestApplication(applicationModel, domainService);
        domainService.init(testApplication, "test");

        return testApplication;
    }


    public void readServerBundle(ApplicationModel applicationModel, NashornScriptEngine nashorn)
    {
        try
        {
            applicationModel.getMetaData().setServerJsBundle(
                nashorn.compile(
                    new FileReader(
                        new File("./src/main/base/resources/js/exceed-server.js")
                    )
                )
            );

            log.info("Read server bundle: " + applicationModel.getMetaData().getServerJsBundle());
        }
        catch (ScriptException | FileNotFoundException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    /**
     * Registers domain types that are referenced from the automatically generated model types thus would
     * produce reference errors when not present.
     *
     * @param applicationModel
     */
    private void readEssentialDomainTypeDefs(ApplicationModel applicationModel)
    {
        try
        {
            DomainType appTranslation = readJSON(
                DomainTypeModel.class,
                new File("./src/main/base/models/domain/AppTranslation.json")
            );

            applicationModel.addDomainType(appTranslation);

        }
        catch (FileNotFoundException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private Map<String, PropertyTypeModel> readBaseProperties()
    {
        try
        {
            Map<String, PropertyTypeModel> map = new HashMap<>();

            final Collection<File> files =
                FileUtils.listFiles(
                    new File("./src/main/base/models/domain/property/"),
                    new SuffixFileFilter(".json"),
                    FalseFileFilter.INSTANCE
                );
            
            for (File file : files)
            {
                PropertyTypeModel propertyType = readJSON(PropertyTypeModel.class, file) ;
                map.put(propertyType.getName(), propertyType);

            }

            return map;
        }
        catch (FileNotFoundException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private <T> T readJSON(Class<T> cls, File file) throws FileNotFoundException
    {

        return JSONParser.defaultJSONParser().parse(
            cls,
            new InputStreamSource(
                new FileInputStream(
                    file
                ),
                true
            )
        );
    }


    public TestApplicationBuilder withDomainRule(DomainRule domainRule)
    {
        domainRules.put(domainRule.getName(), domainRule);
        return this;
    }

    public TestApplicationBuilder withStateMachine(StateMachine stateMachine)
    {
        stateMachines.put(stateMachine.getName(), stateMachine);
        return this;
    }
}
