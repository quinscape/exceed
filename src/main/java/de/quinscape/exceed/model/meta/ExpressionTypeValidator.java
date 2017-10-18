package de.quinscape.exceed.model.meta;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.component.PropType;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.ExpressionValueType;
import de.quinscape.exceed.model.process.DecisionState;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.TypeAnalyzer;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.quinscape.exceed.runtime.util.ExpressionUtil.*;

/**
 * Invokes the type analyzer for every validatable application expressions
 */
public class ExpressionTypeValidator
    implements ModelValidator
{
    private final static TypeAnalyzer typeAnalyzer = new TypeAnalyzer();

    private final static Logger log = LoggerFactory.getLogger(ExpressionTypeValidator.class);


    @Override
    public void validate(ModelValidationContext ctx, ApplicationModel applicationModel)
    {
        validateDefaults(ctx, applicationModel, applicationModel.getApplicationContextModel(), applicationModel.getConfigModel());
        validateDefaults(ctx, applicationModel, applicationModel.getSessionContextModel(), applicationModel
            .getConfigModel());

        for (Process process : applicationModel.getProcesses().values())
        {
            validateDefaults(ctx, applicationModel, process.getContextModel(), process);

            for (ProcessState state : process.getStates().values())
            {
                validateProcessStateExpressions(ctx, applicationModel, state);
            }
        }

        for (View view : applicationModel.getViews().values())
        {
            validateDefaults(ctx, applicationModel, view.getContextModel(), view);
            validateViewExpressions(ctx, applicationModel, view);
        }
    }

    public static boolean shouldValidate(PropType type)
    {
        switch(type)
        {
            case PLAINTEXT:
            case INTEGER:
            case FLOAT:
            case BOOLEAN:
            case MAP:
            case CLASSES:
            case TRANSITION:
            case DOMAIN_TYPE_REFERENCE:
                return true;
            case QUERY_EXPRESSION:
            case CURSOR_EXPRESSION:
            case FILTER_EXPRESSION:
            case VALUE_EXPRESSION:
            case ACTION_EXPRESSION:
            default:
            return false;
        }
    }


    private void validateDefaults(ModelValidationContext ctx, ApplicationModel applicationModel, ContextModel
        contextModel, TopLevelModel topLevelModel)
    {
        if (contextModel != null)
        {
            for (ScopedPropertyModel scopedPropertyModel : contextModel.getProperties().values())
            {
                final ASTExpression defaultValueExpression = scopedPropertyModel.getDefaultValueExpression();
                if (defaultValueExpression != null)
                {
                    TypeAnalyzerContext analyzerContext = new TypeAnalyzerContext(
                        applicationModel,
                        ExpressionType.VALUE,
                        applicationModel.getMetaData().getApplicationDefinitions(),
                        new ExpressionModelContext(topLevelModel, scopedPropertyModel),
                        typeAnalyzer
                    );

                    analyze(ctx, analyzerContext.copy(ExpressionType.VALUE, new ExpressionModelContext(topLevelModel, scopedPropertyModel)), defaultValueExpression);
                }
            }
        }
    }


    private void validateProcessStateExpressions(ModelValidationContext ctx, ApplicationModel applicationModel, ProcessState state)
    {
        if (state instanceof DecisionState)
        {
            final Definitions definitions = applicationModel.lookup(state.getScopeLocation())
                .getLocalDefinitions();
            TypeAnalyzerContext analyzerContext = new TypeAnalyzerContext(applicationModel, ExpressionType.ACTION, definitions, new ExpressionModelContext(state.getProcess()), typeAnalyzer);
            analyze(
                ctx,
                analyzerContext,
                ((DecisionState) state).getDefaultTransition().getActionAST()
            );
        }
    }


    private void validateViewExpressions(ModelValidationContext ctx, ApplicationModel applicationModel, View view)
    {
        final Definitions definitions = applicationModel.lookup(view).getLocalDefinitions();

        TypeAnalyzerContext analyzerContext = new TypeAnalyzerContext(applicationModel, ExpressionType.VALUE, definitions, new ExpressionModelContext(view), typeAnalyzer);

        final ExpressionValue titleAttribute = view.getTitleAttribute();
        if (titleAttribute != null && titleAttribute.getType() == ExpressionValueType.EXPRESSION)
        {
            analyze(ctx, analyzerContext.copy(ExpressionType.VALUE, new ExpressionModelContext(view)), titleAttribute.getAstExpression());
        }

        for (ComponentModel componentModel : view.getContent().values())
        {
            analyzeComponent(ctx, analyzerContext, componentModel, view);
        }
    }

    private void analyze(ModelValidationContext modelValidationContext, TypeAnalyzerContext ctx, ASTExpression astExpression)
    {
        if (astExpression != null)
        {
            try
            {
                typeAnalyzer.analyze(ctx, astExpression);
            }
            catch (Exception e)
            {
                modelValidationContext.registerError(ctx.getContextModel(), astExpression, e);
            }
        }
    }

    private void analyzeComponent(ModelValidationContext modelValidationContext, TypeAnalyzerContext ctx, ComponentModel componentModel, View view)
    {
        final ComponentInstanceRegistration componentRegistration = componentModel.getComponentRegistration();

        final Attributes attrs = componentModel.getAttrs();
        if (attrs != null)
        {
            for (String name : attrs.getNames())
            {
                PropDeclaration propDecl = componentRegistration != null ? componentRegistration.getDescriptor().getPropTypes().get(name) : null;

                final ExpressionType type = getExpressionType(propDecl);
                if (type != ExpressionType.QUERY && type != ExpressionType.FILTER && (propDecl == null || propDecl.isClient()))
                {
                    final ExpressionValue attribute = attrs.getAttribute(name);
                    if (attribute != null)
                    {
                        if (attribute.getType() == ExpressionValueType.EXPRESSION_ERROR)
                        {
                            modelValidationContext.registerError(ctx.getContextModel(), null, attribute.getExpressionError());
                        }
                        else if (attribute.getType() == ExpressionValueType.EXPRESSION)
                        {
                            analyze(modelValidationContext, ctx.copy(type, new ExpressionModelContext(view, componentModel, name)), attribute.getAstExpression());
                        }
                    }
                }
            }
        }

        final ComponentInstanceRegistration registration = componentModel.getComponentRegistration();
        if (registration != null && registration.getVarExpressions().size() > 0)
        {
            for (ASTExpression astExpression : registration.getVarExpressions().values())
            {
                analyze(modelValidationContext, ctx.copy(ExpressionType.VALUE, new ExpressionModelContext(view, componentModel)), astExpression);
            }
        }


        for (ComponentModel kid : componentModel.children())
        {
            analyzeComponent(modelValidationContext, ctx, kid, view);
        }
    }
}
