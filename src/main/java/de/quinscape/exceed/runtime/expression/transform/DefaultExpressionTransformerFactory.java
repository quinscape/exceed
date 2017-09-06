package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.runtime.component.ContextDependencies;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.def.Definitions;

import java.util.ArrayList;
import java.util.List;

/**
 * Static factory class that produces an array of standard expression transformers based on a given context.
 */
public final class DefaultExpressionTransformerFactory
{

    private DefaultExpressionTransformerFactory()
    {
    }

    /**
     * Creates a list of standard expression transformers for the given context
     *
     * @param expressionType        expression type
     * @param applicationModel      application model
     * @param definitions           current definitions (usually retrieved from {@link ScopeDeclarations#getLocalDefinitions()}
     * @param localTransformers     addition transformers
     *
     * @return list of transformers
     */
    public static List<ExpressionTransformer> createTransformers(
        ExpressionType expressionType,
        ApplicationModel applicationModel,
        Definitions definitions,
        ContextDependencies contextDependencies,
        ExpressionTransformer... localTransformers
    )
    {
        final List<ExpressionTransformer> list = new ArrayList<>();

        list.add(new RuleTransformer(applicationModel.getDomainRules()));
        list.add(new ScopeExpressionTransformer(definitions, expressionType, contextDependencies));
        list.add(new DefinitionTransformer(definitions));
        list.add(new DecimalTransformer(applicationModel));
        list.add(new EnumValueTransformer(applicationModel));
        list.add(new StateMachineValueTransformer(applicationModel));
        list.add(new EqualityTransformer());

        for (ExpressionTransformer transformer : localTransformers)
        {
            if (transformer != null)
            {
                list.add(transformer);
            }
        }
        return list;
    }

    /**
     * Creates a list of standard expression transformers for the given context
     *
     * @param expressionType        expression type
     * @param applicationModel      application model
     * @param definitions           current definitions (usually retrieved from {@link ScopeDeclarations#getLocalDefinitions()}
     * @param localTransformers     addition transformers
     *
     * @return list of transformers
     */
    public static List<ExpressionTransformer> createTransformers(
        ExpressionType expressionType,
        ApplicationModel applicationModel,
        Definitions definitions,
        ExpressionTransformer... localTransformers
    )
    {
        return createTransformers(expressionType, applicationModel, definitions, null, localTransformers);
    }
}
