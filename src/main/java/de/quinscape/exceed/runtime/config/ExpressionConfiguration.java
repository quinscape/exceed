package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.action.CustomLogic;
import de.quinscape.exceed.runtime.action.DefaultActionService;
import de.quinscape.exceed.runtime.action.MethodAccessRegistration;
import de.quinscape.exceed.runtime.action.ParameterProviderFactory;
import de.quinscape.exceed.runtime.action.param.ContextPropertyValueProviderFactory;
import de.quinscape.exceed.runtime.action.param.RuntimeContextProviderFactory;
import de.quinscape.exceed.runtime.expression.transform.ActionExpressionTransformer;
import de.quinscape.exceed.runtime.expression.transform.ComponentExpressionTransformer;
import de.quinscape.exceed.runtime.js.DefaultExpressionCompiler;
import de.quinscape.exceed.runtime.js.ExpressionCompiler;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.JsEnvironmentFactory;
import de.quinscape.exceed.runtime.js.JsExpressionRenderer;
import de.quinscape.exceed.runtime.js.TypeAnalyzer;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.js.def.DefinitionsBuilder;
import de.quinscape.exceed.runtime.js.env.CastFunctionRenderer;
import de.quinscape.exceed.runtime.js.env.CastReturnTypeResolver;
import de.quinscape.exceed.runtime.js.env.ContextPropertyTypeResolver;
import de.quinscape.exceed.runtime.js.env.DebugFunctionRenderer;
import de.quinscape.exceed.runtime.js.env.DebugFunctionReturnTypeResolver;
import de.quinscape.exceed.runtime.js.env.NewObjectDefinitionRenderer;
import de.quinscape.exceed.runtime.js.env.NewObjectTypeResolver;
import de.quinscape.exceed.runtime.js.env.PopCursorFunctionRenderer;
import de.quinscape.exceed.runtime.js.env.PropsPropertyTypeResolver;
import de.quinscape.exceed.runtime.js.env.VarsPropertyTypeResolver;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.JsUtil;
import de.quinscape.exceed.runtime.util.Util;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ComponentScan(value = {
    "de.quinscape.exceed.runtime.action.builtin",
}, includeFilters = {
    @ComponentScan.Filter(type = FilterType.ANNOTATION, value = CustomLogic.class)
})

@Configuration
public class ExpressionConfiguration
{
    /** name of the lazyDependency() marker function */
    public static final String LAZY_DEPENDENCY = "lazyDependency";

    private final static Logger log = LoggerFactory.getLogger(ExpressionConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;


    @Bean
    public RuntimeContextProviderFactory runtimeContextProvider()
    {
        return new RuntimeContextProviderFactory();
    }

    @Bean
    public ContextPropertyValueProviderFactory scopedValueProvider()
    {
        return new ContextPropertyValueProviderFactory();
    }
    
    @Bean
    public ActionService actionService()
    {
        final Collection<Object> actionBeans = applicationContext.getBeansWithAnnotation(CustomLogic.class).values();

        final List<ParameterProviderFactory> parameterProviderFactories = new ArrayList<>(
            applicationContext.getBeansOfType(
                ParameterProviderFactory.class,
                false,
                false
            )
            .values()
        );

        // allow providers to use spring bean ordering
        Collections.sort(parameterProviderFactories, AnnotationAwareOrderComparator.INSTANCE);

        log.info("Parameter providers: {}", parameterProviderFactories);

        return new DefaultActionService(
            actionBeans,
            parameterProviderFactories
        );
    }

    @Bean
    public DefaultExpressionCompiler expressionCompiler(NashornScriptEngine nashorn)
    {
        return new DefaultExpressionCompiler(
            nashorn,
            new JsExpressionRenderer(
                Collections.emptyList()
            ),
            new TypeAnalyzer()
        );
    }

    @Bean
    public JsEnvironmentFactory jsEnvironmentFactory(
        ActionService actionService,
        NashornScriptEngine nashorn,
        ExpressionCompiler expressionCompiler
    )
    {
        return new JsEnvironmentFactory(actionService, nashorn, expressionCompiler);
    }

    @Bean
    public Definitions functionDefinitions(ActionService actionService)
    {
        final DefinitionsBuilder builder = Definition.builder();


        builder
            .function("i18n")
            .withDescription("First argument is the translation tag followed by values for its placeholders")
            .withType(DefinitionType.BUILTIN)
            .withParameterModels(
                Arrays.asList(
                    ExpressionUtil.PLAINTEXT_TYPE,
                    ExpressionUtil.OBJECT_TYPE
                ),
                true
            )
            .withReturnType(ExpressionUtil.PLAINTEXT_TYPE)


            .andFunction("transitionModel")
            .withDescription("The transition model for the given name")
            .withType(DefinitionType.BUILTIN)
            .withParameterModels(ExpressionUtil.PLAINTEXT_TYPE)
            .withReturnType(
                DomainProperty.builder()
                    .withType(PropertyType.DOMAIN_TYPE, Model.getType(Transition.class))
                    .build()
            )
            .renderAs("_v.transition")


            .andFunction("uri")
            .withDescription("Inner-application URI function, takes a template and map of template values as arguments")
            .withType(DefinitionType.BUILTIN)
            .withParameterModels(
                ExpressionUtil.PLAINTEXT_TYPE,
                DomainProperty.builder()
                    .withType(PropertyType.MAP, PropertyType.OBJECT)
                    .build()
            )
            .withReturnType(ExpressionUtil.PLAINTEXT_TYPE)
            .renderAs("_v.uri")


            .andFunction("param")
            .withDescription("Mapping parameter")
            .withType(DefinitionType.BUILTIN)
            .withParameterModels(ExpressionUtil.PLAINTEXT_TYPE)
            .withReturnType(ExpressionUtil.PLAINTEXT_TYPE)
            .renderAs("_v.param")


            .andFunction("now")
            .withDescription("Returns a timestamp for the current time.")
            .withType(DefinitionType.BUILTIN)
            .withParameterModels(ExpressionUtil.PLAINTEXT_TYPE)
            .withDescription("The current timestamp")
            .withReturnType(PropertyType.TIMESTAMP)
            .renderAs("_now")


            .andFunction("debug")
            .withType(DefinitionType.BUILTIN)
            .withDescription("Helper function that will debug log its arguments and return the first argument")
            .withParameterModels(ExpressionUtil.OBJECT_TYPE)
            .withReturnTypeResolver(new DebugFunctionReturnTypeResolver())
            .renderWith(new DebugFunctionRenderer())


            .andFunction("cast")
            .withType(DefinitionType.BUILTIN)
            .withDescription(
                "Casts a value to another type if possible. " +
                "Useful to e.g. convert one Enum to another or an Integer to an Enum."
            )
            .withParameterModels(ExpressionUtil.PLAINTEXT_TYPE, ExpressionUtil.OBJECT_TYPE)
            .withReturnTypeResolver(new CastReturnTypeResolver())
            .renderWith(new CastFunctionRenderer());


        formDefs(builder);
        securityDefs(builder);
        actionDefs(builder);
        componentDefs(builder);
        domainObjectDefs(builder);

        builder.merge(actionService.getActionFunctionDefinitions());
            
        final Definitions definitions = builder.build();

        if (log.isDebugEnabled())
        {
            final ArrayList<Definition> list = new ArrayList<>(definitions.getDefinitions().values());
            Collections.sort(list);
            log.debug(
                "-- Base Definitions:\n" +
                    "{}\n\n" +
                    "-- Base Definitions End",
                Util.join(list, "\n")
            );
        }

        return definitions;
    }


    private void domainObjectDefs(DefinitionsBuilder builder)
    {
        builder
            .chapter("Domain Object related expressions")

            .function("newObject")
            .withType(DefinitionType.BUILTIN)
            .withParameterModels(ExpressionUtil.PLAINTEXT_TYPE)
            .withDescription("Create a new object of the given type")
            .withReturnTypeResolver(new NewObjectTypeResolver())
            .renderWith(new NewObjectDefinitionRenderer())

            .andFunction("isNew")
            .withDescription("Checks if the given domain object is new i.e. has a null identity")
            .withType(DefinitionType.BUILTIN)
            .withParameterModels(ExpressionUtil.GENERIC_DOMAIN_TYPE)
            .withReturnType(ExpressionUtil.BOOLEAN_TYPE)
            .renderAs("_v.isNew")

            .andFunction("uuid")
            .withDescription("Creates a new UUID type 4 value")
            .withType(DefinitionType.BUILTIN)
            .withReturnType(ExpressionUtil.PLAINTEXT_TYPE)
            .renderAs("_v.uuid");
    }


    private void componentDefs(DefinitionsBuilder builder)
    {
        builder
            .chapter("Component Expressions")
            
            .identifier(ComponentExpressionTransformer.PROPS_IDENTIFIER)
            .withType(DefinitionType.BUILTIN)
            .withDescription("In a component expression, the props/attributes of the component (e.g. props.name)")
            .withPropertyTypeResolver(new PropsPropertyTypeResolver())


            .andIdentifier(ComponentExpressionTransformer.VARS_IDENTIFIER)
            .withType(DefinitionType.BUILTIN)
            .withDescription("In a component expression, the current vars of the component")
            .withPropertyTypeResolver(new VarsPropertyTypeResolver())


            .andIdentifier(ComponentExpressionTransformer.MODEL_IDENTIFIER)
            .withType(DefinitionType.BUILTIN)
            .withDescription("A reference to the current model")
            .withPropertyType(DomainProperty.builder()
                .withType(PropertyType.DOMAIN_TYPE, Model.getType(ComponentModel.class))
                .build())


            .andIdentifier(ComponentExpressionTransformer.CONTEXT_IDENTIFIER)
            .withType(DefinitionType.BUILTIN)
            .withDescription("The current cursor context or transition context.")
            .withPropertyTypeResolver(new ContextPropertyTypeResolver())


            .andFunction(LAZY_DEPENDENCY)
            .withType(DefinitionType.BUILTIN)
            .withDescription(
                "Marks first the expression argument as being lazily evaluated in terms of context dependency updates." +
                    "The user can change the context variables within without it triggering view updates."
            )
            .withParameterModels(ExpressionUtil.OBJECT_TYPE)
            .withReturnType(ExpressionUtil.OBJECT_TYPE)
            // eliminate function at runtime, only render as pair of brackets
            .renderAs("");

    }


    private void actionDefs(DefinitionsBuilder builder)
    {
        /**
         * when().then().else() is handled directly in {@link ActionExpressionTransformer)
         */

        builder
            .chapter(DefaultActionService.CHAPTER_ACTION)

            .function(ActionExpressionTransformer.FORK_ACTION)
            .withType(DefinitionType.BUILTIN)
            .withParameterModels(Collections.singletonList(ExpressionUtil.OBJECT_TYPE), true)
            .withDescription(
                "Special action function that takes multiple sequences as input, runs them in parallel and waits for " +
                    "all of them to end.")
            .restrictTo(ExpressionType.ACTION)
            .withReturnType(
                DomainProperty.builder()
                    .withType(PropertyType.LIST, PropertyType.OBJECT)
                    .build()
            )


            .andFunction(ActionExpressionTransformer.WHEN_ACTION)
            .withParameterModels(ExpressionUtil.OBJECT_TYPE)
            .withType(DefinitionType.BUILTIN)
            .withDescription("A conditional branch in the action promise chain")
            .restrictTo(ExpressionType.ACTION)
            .withReturnType(MethodAccessRegistration.VOID_TYPE)

            .andFunction(ActionExpressionTransformer.CATCH_ACTION)
            .withParameterModels(ExpressionUtil.OBJECT_TYPE)
            .withType(DefinitionType.BUILTIN)
            .withDescription("Allows to declare a sub action sequence to execute as rejection handler")
            .restrictTo(ExpressionType.ACTION)
            .withReturnType(MethodAccessRegistration.VOID_TYPE);
    }


    private void securityDefs(DefinitionsBuilder builder)
    {

        builder
            .chapter("security expressions")
            .function("isAdmin")
            .withDescription("Checks if the current user has the admin role. Same as hasRole('ROLE_ADMIN').")
            .withType(DefinitionType.BUILTIN)
            .withReturnType(ExpressionUtil.BOOLEAN_TYPE)
            .renderAs("_v.isAdmin")


            .andFunction("hasRole")
            .withDescription("Checks if the current user has the given role.")
            .withParameterModels(ExpressionUtil.PLAINTEXT_TYPE)
            .withType(DefinitionType.BUILTIN)
            .withReturnType(ExpressionUtil.BOOLEAN_TYPE)
            .renderAs("_v.hasRole");
    }


    private void formDefs(DefinitionsBuilder builder)
    {
        builder
            .chapter("Form related expressions")
            
            .function("fieldId")
            .withDescription("Returns the correct field id for fields in iterative contexts")
            .withParameterModels(ExpressionUtil.PLAINTEXT_TYPE)
            .withType(DefinitionType.BUILTIN)
            .withReturnType(ExpressionUtil.PLAINTEXT_TYPE)
            .renderAs("_v.fields")

            .andFunction("popCursor")
            .withType(DefinitionType.BUILTIN)
            .withDescription(
                "Shortens the path of a cursor by n levels, returning the cursor location to a parent or grandparent " +
                    "of the current location.")
            .withParameterModels(ExpressionUtil.OBJECT_TYPE, ExpressionUtil.INTEGER_TYPE)
            .withReturnType(ExpressionUtil.OBJECT_TYPE)
            .renderWith(new PopCursorFunctionRenderer());

    }


    @Bean
    public NashornScriptEngine nashornScriptEngine()
    {
        return JsUtil.createEngine();
    }

}
