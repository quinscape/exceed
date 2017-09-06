package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTPropertyChainDot;
import de.quinscape.exceed.expression.ASTPropertyChainSquare;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.component.ContextDependencies;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.js.def.IdentifierDefinition;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;

/**
 * Transforms RHS scope references to a runtime view API call
 */
public class ScopeExpressionTransformer
    implements ExpressionTransformer
{
    private final Definitions definitions;

    private final ExpressionType expressionType;

    private final ContextDependencies contextDependencies;


    public ScopeExpressionTransformer(
        Definitions definitions, ExpressionType expressionType,
        ContextDependencies contextDependencies
    )
    {
        this.definitions = definitions;
        this.expressionType = expressionType;
        this.contextDependencies = contextDependencies;
    }


    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        if (node instanceof ASTPropertyChain)
        {
            final Node child = node.jjtGetChild(0);
            return child instanceof ASTIdentifier && isScopeReference((ASTIdentifier) child);
        }
        return node instanceof ASTIdentifier && isScopeReference((ASTIdentifier) node);
    }

    private final static SingleQuoteJSONGenerator generator = SingleQuoteJSONGenerator.INSTANCE;

    private boolean isScopeReference(ASTIdentifier node)
    {
        if (definitions == null)
        {
            return false;
        }

        final Definition definition = definitions.getDefinition(node.getName());
        return definition instanceof IdentifierDefinition &&
               definition.getDefinitionType() == DefinitionType.CONTEXT;
    }


    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        if (expressionType == ExpressionType.CURSOR)
        {
            ctx.output("_v.scopeCursor(");
        }
        else
        {
            ctx.output("_v.scope(");
        }
        final String contextName;
        if (node instanceof ASTPropertyChain)
        {
            final ASTIdentifier first = (ASTIdentifier) node.jjtGetChild(0);

            contextName = first.getName();

            if (contextName.equals("transitionContext"))
            {
                if (!(ctx.getModelContext().getFineDetail() instanceof Transition))
                {
                    throw new InvalidExpressionException("transitionContext only valid in transition action expressions");
                }

                final Node second = node.jjtGetChild(1).jjtGetChild(0);
                if (second instanceof ASTFunction)
                {

                    final String functionName = ((ASTFunction) second).getName();
                    if (functionName.equals("extract"))
                    {
                        if (second.jjtGetNumChildren() != 1)
                        {
                            throw new InvalidExpressionException("transitionContext.extract(typeOrName) takes exactly one string argument (name of domain type or query name)");
                        }

                        ctx.output("__transitionContextExtract(");
                        ctx.applyRecursive(second.jjtGetChild(0));
                        ctx.output(")");
                        return;
                    }
                }
            }


            final IdentifierDefinition definition = (IdentifierDefinition) definitions.getDefinition(contextName);

            ctx.output("[");
            final int numChildren = node.jjtGetNumChildren();


            ctx.output(generator.quote(contextName));

            for (int i = 1; i < numChildren; i++)
            {
                ctx.output(",");

                Node chainLink = node.jjtGetChild(i);
                final Node child = chainLink.jjtGetChild(0);

                if (chainLink instanceof ASTPropertyChainDot)
                {
                    if (child instanceof ASTIdentifier)
                    {
                        ctx.output(
                            generator.quote(
                                ((ASTIdentifier) child).getName()
                            )
                        );
                    }
                    else if (child instanceof ASTFunction)
                    {
                        ctx.applyRecursive(child);
                    }
                    else
                    {
                        throw new IllegalStateException("Unexpected property accessor child: " + child);
                    }
                }
                else if (chainLink instanceof ASTPropertyChainSquare)
                {
                    ctx.applyRecursive(child);
                }
                else
                {
                    throw new IllegalStateException("Unexpected property chain link: " + chainLink);
                }

            }
            ctx.output("]");
        }
        else
        {
            contextName = ((ASTIdentifier) node).getName();
            ctx.output(generator.quote(contextName));
        }

        ctx.output(")");

        final Object contextModel = ctx.getModelContext().getContextModel();
        if (contextDependencies != null)
        {
            if (contextModel instanceof ComponentModel && !ExpressionUtil.isLazyDependency(node))
            {
                final String varName = (String) ctx.getModelContext().getFineDetail();
                contextDependencies.registerDependency(
                    contextName,
                    ((ComponentModel) contextModel).getComponentId(),
                    varName
                );
            }
        }
    }
}
