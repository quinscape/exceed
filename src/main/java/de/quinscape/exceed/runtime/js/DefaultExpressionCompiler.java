package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.process.DecisionModel;
import de.quinscape.exceed.model.process.DecisionState;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.component.ContextDependencies;
import de.quinscape.exceed.runtime.expression.transform.ActionExpressionTransformer;
import de.quinscape.exceed.runtime.expression.transform.DefaultExpressionTransformerFactory;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultExpressionCompiler
    implements ExpressionCompiler
{

    private final static Logger log = LoggerFactory.getLogger(DefaultExpressionCompiler.class);


    private final NashornScriptEngine nashorn;

    private final JsExpressionRenderer expressionRenderer;

    private final TypeAnalyzer typeAnalyzer;


    public DefaultExpressionCompiler(
        NashornScriptEngine nashorn,
        JsExpressionRenderer expressionRenderer,
        TypeAnalyzer typeAnalyzer
    )
    {
        this.nashorn = nashorn;
        this.expressionRenderer = expressionRenderer;
        this.typeAnalyzer = typeAnalyzer;
    }


    @Override
    public ExpressionBundle compile(ApplicationModel applicationModel)
    {
        ScriptBuffer scriptBuffer = new ScriptBuffer();

        final Definitions systemDefinitions = applicationModel.getMetaData().getSystemDefinitions();
        final Definitions systemRoleDefinitions = applicationModel.lookup(ScopeMetaModel.SYSTEM).getLocalDefinitions();

        compileDefaults(
            applicationModel, scriptBuffer, systemDefinitions, "app", applicationModel.getConfigModel().getApplicationContextModel(),
            applicationModel.getConfigModel()
        );
        compileDefaults(
            applicationModel, scriptBuffer, systemDefinitions, "session", applicationModel.getConfigModel().getSessionContextModel(),
            applicationModel.getConfigModel()
        );
        compileDefaults(
            applicationModel, scriptBuffer, systemDefinitions, "user", applicationModel.getConfigModel().getUserContextModel(),
            applicationModel.getConfigModel()
        );

        for (DomainType domainType : applicationModel.getDomainTypes().values())
        {
            final Collection<DomainProperty> properties;

            if (domainType instanceof QueryTypeModel)
            {
                final QueryTypeModel queryTypeModel = (QueryTypeModel) domainType;
                properties = queryTypeModel.getParameterMap().values();

                final ExpressionValue countValue = queryTypeModel.getCountValue();
                if (countValue != null)
                {
                    renderValueExpression(
                        applicationModel,
                        systemRoleDefinitions,
                        countValue.getAstExpression(),
                        new ExpressionModelContext(domainType, "count"),
                        "query_type_count_" + domainType.getName(),
                        scriptBuffer
                    );
                }
            }
            else
            {
                properties = domainType.getProperties();
            }

            for (DomainProperty property : properties)
            {
                final ASTExpression defaultValueExpression = property.getDefaultValueExpression();
                if (defaultValueExpression != null)
                {
                    renderValueExpression(
                        applicationModel,
                        systemRoleDefinitions,
                        defaultValueExpression,
                        new ExpressionModelContext(domainType, property),
                        "default_domain_" + domainType.getName() + "_" + property.getName(),
                        scriptBuffer
                    );
                }
            }
        }

        for (DomainRule domainRule : applicationModel.getDomainRules().values())
        {
            renderRuleExpression(
                applicationModel,
                systemRoleDefinitions,
                domainRule,
                "rule_" + domainRule.getName(),
                scriptBuffer
            );
        }


        for (Process process : applicationModel.getProcesses().values())
        {
            final ContextModel contextModel = process.getContextModel();

            compileDefaults(
                applicationModel,
                scriptBuffer,
                systemRoleDefinitions,
                process.getName(),
                contextModel,
                process
            );

            validateProcessExpressions(applicationModel, scriptBuffer, process);
        }

        for (View view : applicationModel.getViews().values())
        {
            validateViewExpressions(applicationModel, scriptBuffer, view);
            compileVarDefaults(view, applicationModel, scriptBuffer);
        }

        final ExpressionBundle expressionBundle = ExpressionBundle.fromResults(
            nashorn,
            applicationModel.getVersion(),
            scriptBuffer
        );

        log.debug("Compiled expressions {}: {}", expressionBundle.getVersion(), expressionBundle.getSource());

        return expressionBundle;
    }


    private void compileVarDefaults(
        View view,
        ApplicationModel applicationModel,
        ScriptBuffer scriptBuffer
    )
    {
        ContextDependencies contextDependencies = new ContextDependencies(new HashMap<>());

        final Definitions localDefinitions = applicationModel.lookup(view).getLocalDefinitions();
        for (ComponentModel componentModel : view.getContent().values())
        {
            compileVarDefaultsRecursive(
                view,
                componentModel,
                applicationModel,
                localDefinitions,
                scriptBuffer,
                contextDependencies
            );
        }

        view.setContextDependencies(contextDependencies);
    }


    private void compileVarDefaultsRecursive(
        View view, ComponentModel componentModel,
        ApplicationModel applicationModel, Definitions localDefinitions, ScriptBuffer scriptBuffer,
        ContextDependencies contextDependencies
    )
    {
        final ComponentInstanceRegistration registration = componentModel.getComponentRegistration();
        final String componentName = componentModel.getName();
        final String componentId = componentModel.getComponentId();
        if (registration != null)
        {
            final Map<String, ASTExpression> vars = registration.getVarExpressions();

            for (Map.Entry<String, ASTExpression> e : vars.entrySet())
            {
                final String varName = e.getKey();
                final ASTExpression astExpression = e.getValue();

                renderVarDefaultValueExpression(
                    applicationModel,
                    localDefinitions,
                    astExpression,
                    new ExpressionModelContext(view, componentModel, varName),
                    "component_" + componentName + "_" + componentId + "_var_" + varName,
                    scriptBuffer,
                    contextDependencies
                );
            }
        }

        for (ComponentModel model : componentModel.children())
        {
            compileVarDefaultsRecursive(
                view, model,
                applicationModel,
                localDefinitions,
                scriptBuffer,
                contextDependencies
            );
        }
    }


    private void validateViewExpressions(
        ApplicationModel applicationModel, ScriptBuffer scriptBuffer, View view
    )
    {
        final Definitions definitions = applicationModel.lookup(view).getLocalDefinitions();

        compileDefaults(
            applicationModel,
            scriptBuffer,
            definitions,
            "view",
            view.getContextModel(),
            view
        );
    }


    private void validateProcessExpressions(
        ApplicationModel applicationModel, ScriptBuffer scriptBuffer, Process process
    )
    {

        final Transition startTransition = process.getStartTransition();
        renderTransitionAction(
            applicationModel,
            applicationModel.lookup(process.getScopeLocation()).getLocalDefinitions(),
            startTransition.getActionAST(),
            new ExpressionModelContext(process, null, startTransition),
            "transition_" + process.getName() + "_start",
            scriptBuffer
        );

        final String defaultName = process.getName();

        for (ProcessState state : process.getStates().values())
        {
            final String scopeLocation = state.getScopeLocation();

            final Definitions definitions = applicationModel.lookup(scopeLocation).getLocalDefinitions();


            if (state instanceof DecisionState)
            {
                String name = process.getName() + "_" + state.getName();


                final Transition defaultTransition = ((DecisionState) state).getDefaultTransition();
                final String decisionNameBase = "decision_" + name;
                renderTransitionAction(
                    applicationModel,
                    definitions,
                    defaultTransition.getActionAST(),
                    new ExpressionModelContext(process, state, defaultTransition),
                    decisionNameBase + "_tdef",
                    scriptBuffer
                );

                List<DecisionModel> decisions = ((DecisionState) state).getDecisions();
                for (int i = 0; i < decisions.size(); i++)
                {
                    DecisionModel decisionModel = decisions.get(i);
                    renderValueExpression(
                        applicationModel,
                        definitions,
                        decisionModel.getExpressionAST(),
                        new ExpressionModelContext(process, state, "test-" + i),
                        decisionNameBase + "_expr" + i,
                        scriptBuffer
                    );
                    renderTransitionAction(
                        applicationModel,
                        definitions,
                        decisionModel.getTransition().getActionAST(),
                        new ExpressionModelContext(process, state, decisionModel.getTransition()),
                        decisionNameBase + "_t" + i,
                        scriptBuffer
                    );
                }
            }
            else if (state instanceof ViewState)
            {
                String name = process.getProcessStateName(state.getName());

                final Map<String, Transition> transitions = ((ViewState) state).getTransitions();
                if (transitions != null)
                {
                    for (Transition transition : transitions.values())
                    {
                        renderTransitionAction(
                            applicationModel,
                            definitions,
                            transition.getActionAST(),
                            new ExpressionModelContext(process, state, transition),
                            "action_" + name + "_t_" + transition.getName(),
                            scriptBuffer
                        );
                    }
                }
            }
        }
    }


    private void compileDefaults(
        ApplicationModel applicationModel, ScriptBuffer scriptBuffer, Definitions definitions,
        String defaultName, ContextModel contextModel, TopLevelModel topLevelModel
    )
    {
        if (contextModel != null)
        {
            for (ScopedPropertyModel scopedPropertyModel : contextModel.getProperties().values())
            {
                compileDefault(
                    applicationModel,
                    scriptBuffer,
                    definitions,
                    defaultName,
                    topLevelModel,
                    scopedPropertyModel
                );
            }
        }
    }


    private void compileDefault(
        ApplicationModel applicationModel,
        ScriptBuffer scriptBuffer,
        Definitions definitions,
        String defaultName,
        TopLevelModel topLevelModel,
        ScopedPropertyModel scopedPropertyModel
    )
    {
        final ASTExpression defaultExpression = scopedPropertyModel
            .getDefaultValueExpression();
        if (defaultExpression != null)
        {
            renderValueExpression(
                applicationModel,
                definitions,
                defaultExpression,
                new ExpressionModelContext(topLevelModel, scopedPropertyModel),
                "default_" + defaultName + "_ " + scopedPropertyModel.getName(),
                scriptBuffer
            );
        }
    }


    private void renderTransitionAction(
        ApplicationModel applicationModel,
        Definitions definitions,
        ASTExpression astExpression,
        ExpressionModelContext expressionModelContext,
        String idBase,
        ScriptBuffer scriptBuffer
    )
    {
        if (astExpression != null)
        {
            final ExpressionType expressionType = ExpressionType.ACTION;
            compileExpression(
                new TypeAnalyzerContext(
                    applicationModel,
                    expressionType,
                    definitions,
                    expressionModelContext,
                    typeAnalyzer
                ),
                scriptBuffer,
                idBase,
                astExpression,
                () ->
                    "function()\n" +
                        "{\n" +
                        "    return " + expressionRenderer.transform(
                        applicationModel,
                        expressionType,
                        expressionModelContext,
                        astExpression,
                        DefaultExpressionTransformerFactory.createTransformers(
                            expressionType,
                            applicationModel,
                            definitions,
                            new ActionExpressionTransformer(definitions, true)
                        ),
                        scriptBuffer
                    ) + ";\n" +
                        "}"
            );
        }
    }


    private void renderValueExpression(
        ApplicationModel applicationModel,
        Definitions definitions,
        ASTExpression astExpression,
        ExpressionModelContext expressionContextModel,
        String idBase,
        ScriptBuffer scriptBuffer
    )
    {
        final ExpressionType expressionType = ExpressionType.VALUE;

        compileExpression(
            new TypeAnalyzerContext(
                applicationModel,
                expressionType,
                definitions,
                expressionContextModel,
                typeAnalyzer
            ),
            scriptBuffer,
            idBase,
            astExpression,
            () ->
                "function()\n" +
                    "{\n" +
                    "    return " + expressionRenderer.transform(
                    applicationModel, expressionType,
                    expressionContextModel,
                    astExpression,
                    DefaultExpressionTransformerFactory.createTransformers(expressionType, applicationModel,
                        definitions
                    ),
                    scriptBuffer
                ) + ";\n" +
                    "}"
        );
    }


    private void renderVarDefaultValueExpression(
        ApplicationModel applicationModel,
        Definitions definitions,
        ASTExpression astExpression,
        ExpressionModelContext expressionContextModel,
        String idBase,
        ScriptBuffer scriptBuffer,
        ContextDependencies contextDependencies
    )
    {
        final ExpressionType expressionType = ExpressionType.VALUE;
        compileExpression(
            new TypeAnalyzerContext(
                applicationModel,
                expressionType,
                definitions,
                expressionContextModel,
                typeAnalyzer
            ),
            scriptBuffer,
            idBase,
            astExpression,
            () ->
                "function()\n" +
                    "{\n" +
                    "    return " + expressionRenderer.transform(
                    applicationModel, expressionType,
                    expressionContextModel,
                    astExpression,
                    DefaultExpressionTransformerFactory.createTransformers(expressionType, applicationModel,
                        definitions, contextDependencies
                    ),
                    scriptBuffer
                ) + ";\n" +
                    "}"
        );

    }


    private void renderRuleExpression(
        ApplicationModel applicationModel,
        Definitions definitions,
        DomainRule domainRule,
        String idBase,
        ScriptBuffer scriptBuffer
    )
    {
        log.debug("Render rule expression for {}: {}", domainRule.getName(), domainRule.getRule());

        final ASTExpression astExpression = domainRule.getRuleValue().getAstExpression();
        final ExpressionType expressionType = ExpressionType.RULE;
        compileExpression(
            new TypeAnalyzerContext(
                applicationModel,
                expressionType,
                Definition.builder()
                    .merge(applicationModel.getMetaData().getApplicationDefinitions())
                    .identifier("value")
                    .withPropertyType(domainRule.getTarget())
                    .withType(DefinitionType.RULE)
                    .build(),
                new ExpressionModelContext(domainRule), typeAnalyzer
            ),
            scriptBuffer, idBase, astExpression,
            () ->
                "function(value)\n" +
                    "{\n" +
                    "    return " +
                    expressionRenderer.transform(
                        applicationModel, expressionType,
                        new ExpressionModelContext(domainRule),
                        astExpression,
                        DefaultExpressionTransformerFactory
                            .createTransformers(expressionType, applicationModel, definitions),
                        scriptBuffer
                    ) +
                    ";\n" +
                    "}"
        );

    }


    /**
     * Checks if the the given ast expression has a compilation result attached to it. If such an attachment exists,
     * it is returned, otherwise the given supplier is consulted for a new compilation result.
     *
     * @param idBase        prefix for the unique expression identifier
     * @param astExpression ast expression
     * @param supplier      supplier
     */
    private void compileExpression(
        TypeAnalyzerContext context,
        ScriptBuffer scriptBuffer,
        String idBase,
        ASTExpression astExpression,
        Supplier<String> supplier
    )
    {
        try
        {
            final String existing = astExpression.annotation().getCompilationResult();
            if (existing != null)
            {
                // -> reuse existing compilation
                return;
            }

            typeAnalyzer.analyze(context, astExpression);

            String compiled = supplier.get();

            final String identifier = scriptBuffer.addCodeBlock(idBase, compiled);
            astExpression.annotation().setIdentifier(identifier);
        }
        catch (Exception e)
        {
            throw new ExpressionCompilationException(
                "Error compiling expression: " + context.getContextModel(), e);
        }
    }
}
