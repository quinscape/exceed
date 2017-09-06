package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTExpressionSequence;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTPropertyChainDot;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.InvalidReferenceException;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.js.def.FunctionDefinition;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import de.quinscape.exceed.runtime.util.AssignmentReplacementException;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;

public class ActionExpressionTransformer
    implements ExpressionTransformer
{
    /**
     * Name of the special fork() action
     */
    public static final String FORK_ACTION = "fork";

    private static final String PARAM_PREFIX = "__";

    public static final String WHEN_ACTION = "when";
    
    public static final String THEN = "then";
    public static final String ELSE = "else";

    public static final String CATCH_ACTION = "catch";

    private final Definitions definitions;

    private final boolean serverside;


    public ActionExpressionTransformer(Definitions definitions, boolean serverside)
    {
        this.definitions = definitions;
        this.serverside = serverside;
    }

    public final static SingleQuoteJSONGenerator generator = SingleQuoteJSONGenerator.INSTANCE;

    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        return ctx.getExpressionType() == ExpressionType.ACTION && node instanceof ASTExpression && node.jjtGetParent() == null;
    }

    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        Node first = node.jjtGetChild(0);

        transformActionOrSequence(ctx, first);
    }


    private void transformActionOrSequence(ExpressionTransformationContext ctx, Node first)
    {
        if (first instanceof ASTExpressionSequence)
        {
            renderSequence(ctx, first);
        }
        else
        {
            renderActionExpression(ctx, first, true,false, false);
        }
    }


    public void renderSequence(ExpressionTransformationContext ctx, Node first)
    {
        final int count = first.jjtGetNumChildren();
        final int last = count - 1;
        boolean isFirst = true;
        for (int i = 0; i < count; i++)
        {
            final boolean hasNext = i < last;

            boolean nextIsCatch = false;
            if (hasNext)
            {
                final Node node = first.jjtGetChild(i + 1);
                if (node instanceof ASTFunction && ((ASTFunction) node).getName().equals(CATCH_ACTION))
                {
                    nextIsCatch = true;
                }
            }


            boolean generatedAction = renderActionExpression(ctx, first.jjtGetChild(i), isFirst, hasNext, nextIsCatch);
            if (generatedAction)
            {
                isFirst = false;
            }
        }
    }

    private boolean renderActionExpression(
        ExpressionTransformationContext ctx, Node node, boolean isFirst, boolean hasNext, boolean nextIsCatch
    )
    {

        ASTIdentifier identifier = null;
        ASTPropertyChain propertyChain = null;

        boolean hasResultAssignment = false;

        if (node instanceof ASTAssignment)
        {
            Node lft = node.jjtGetChild(0);
            Node rgt = node.jjtGetChild(1);

            if (lft instanceof ASTIdentifier)
            {
                identifier = (ASTIdentifier) lft;
                hasResultAssignment = true;
            }
            else if (lft.jjtGetChild(0) instanceof ASTIdentifier)
            {
                identifier = (ASTIdentifier) lft.jjtGetChild(0);
                propertyChain = (ASTPropertyChain) lft;
                hasResultAssignment = true;
            }
            else
            {
                throw new AssignmentReplacementException("Assignment left side must start with an identifier : " +
                    ExpressionRenderer.render(node));
            }

            node = rgt;
        }

        final FunctionDefinition actionDefinition = getActionDefinition(node);
        if (actionDefinition != null)
        {
            final String name = ((ASTFunction) node).getName();
            if (name.equals(FORK_ACTION))
            {
                renderFork(ctx, node);
            }
            else if (name.equals(WHEN_ACTION))
            {
                renderWhen(ctx, node);
            }
            else if (name.equals(CATCH_ACTION))
            {
                renderCancel(ctx, node);
            }
            else
            {
                ctx.output("_a.action(");
                ctx.output(generator.quote(name));
                ctx.output(",[");

                final int numChildren = node.jjtGetNumChildren();
                for(int i = 0; i < numChildren; i++)
                {
                    if (i > 0)
                    {
                        ctx.output(",");
                    }

                    if (actionDefinition.getParameterModel(i).equals(ExpressionUtil.EXPRESSION_TYPE))
                    {
                        final String quotedExpression = SingleQuoteJSONGenerator.INSTANCE.quote(
                            ExpressionRenderer.render(node.jjtGetParent() instanceof ASTPropertyChain ? node.jjtGetParent() : node)
                        );
                        final String varName = ctx.pushCodeBlock(
                            "expr",
                            "__Expression(" + quotedExpression + ")"
                        );

                        ctx.output( varName );
                    }
                    else
                    {
                        final Node kid = node.jjtGetChild(i);
                        ctx.applyRecursive(kid);
                    }
                }
                ctx.output("])");
            }
            if (!isFirst)
            {
                ctx.output("})");
            }

            handleAssignment(ctx, hasNext, nextIsCatch, identifier, propertyChain, hasResultAssignment);
            return true;
        }
        else if ( node instanceof ASTPropertyChain && node.jjtGetChild(0) instanceof ASTFunction)
        {
            ASTFunction fn = (ASTFunction)node.jjtGetChild(0);
            if (fn.getName().equals(WHEN_ACTION))
            {
                if (node.jjtGetNumChildren() > 3)
                {
                    throw new InvalidExpressionException("Invalid when expression: " + ExpressionUtil.renderExpressionOf(node));
                }

                ctx.output("_v.when(");

                transformActionOrSequence(ctx, fn);

                if (node.jjtGetNumChildren() > 1)
                {
                    final Node second = node.jjtGetChild(1).jjtGetChild(0);
                    if (!(second instanceof ASTFunction) || !((ASTFunction) second).getName().equals(THEN))
                    {
                        throw new InvalidExpressionException("Invalid when expression: " + ExpressionUtil.renderExpressionOf(second));
                    }

                    ctx.output(",function(){ return ");

                    transformActionOrSequence(ctx, second.jjtGetChild(0));
                    ctx.output("}");

                    if (node.jjtGetNumChildren() > 2)
                    {
                        final Node third = node.jjtGetChild(2).jjtGetChild(0);

                        if (!(third instanceof ASTFunction) || !((ASTFunction) third).getName().equals(ELSE))
                        {
                            throw new InvalidExpressionException("Invalid when expression: " + ExpressionUtil.renderExpressionOf(third));
                        }
                        ctx.output(",function(){ return ");
                        transformActionOrSequence(ctx, third.jjtGetChild(0));
                        ctx.output("}");
                    }
                    ctx.output(")");
                }

                if (hasNext)
                {
                    ctx.output("." + ( nextIsCatch ? "catch" : "then") + "(function(){ return ");
                }
                else if (!isFirst)
                {
                    ctx.output("})");
                }
                return true;
            }
            else
            {
                final FunctionDefinition fnDef = getActionDefinition(fn);
                if (fnDef != null && fnDef.embedsForeignExpression())
                {
                    renderActionExpression(ctx, fn, true, false, false);
                    if (!isFirst)
                    {
                        ctx.output("})");
                    }
                    handleAssignment(ctx, hasNext, nextIsCatch, identifier, propertyChain, hasResultAssignment);
                    return true;
                }
            }
            return false;
        }
        else if (hasResultAssignment)
        {
            ctx.output("_v.updateScope([");

            final String identifierName = identifier.getName();
            ctx.output(generator.quote(identifierName));
            if (propertyChain != null)
            {
                renderPropChainRestAsPath(ctx, propertyChain);
            }
            ctx.output("], ");

            ctx.applyRecursive(node);

            ctx.output(")");

            if (isFirst && hasNext)
            {
                // special case where we have an assignment as first sequence node, we generate a comma operator to
                // first execute the preceding updateScope but then evaluate to the next action promise as return value
                ctx.output(",");
            }

            if (!isFirst)
            {
                if (hasNext)
                {
                    ctx.output("})." + ( nextIsCatch ? "catch" : "then") + "(function(){ return ");
                }
                else
                {
                    ctx.output("})");
                }
            }
            return false;
        }



        throw new InconsistentModelException("Invalid action expression: " + ExpressionUtil.renderExpressionOf(node));
    }


    private void handleAssignment(
        ExpressionTransformationContext ctx, boolean hasNext, boolean nextIsCatch, ASTIdentifier identifier,
        ASTPropertyChain propertyChain,
        boolean hasResultAssignment
    )
    {
        if (hasResultAssignment || hasNext)
        {
            ctx.output("." + ( nextIsCatch ? "catch" : "then") + "(function(");
            if (hasResultAssignment)
            {
                final String identifierName = identifier.getName();
                final String paramName = PARAM_PREFIX + identifierName;
                ctx.output(paramName + ") {_v.updateScope([");
                ctx.output(generator.quote(identifierName));

                if (propertyChain != null)
                {
                    renderPropChainRestAsPath(ctx, propertyChain);
                }
                ctx.output("], " + PARAM_PREFIX + identifierName + ");");
            }
            else
            {
                ctx.output("){");
            }

            if (hasNext)
            {
                ctx.output("return ");
            }
            else
            {
                ctx.output("})");
            }
        }
    }


    private void renderPropChainRestAsPath(ExpressionTransformationContext ctx, ASTPropertyChain propertyChain)
    {
        final int numChildren = propertyChain.jjtGetNumChildren();

        for (int i=1; i < numChildren; i++)
        {
            ctx.output(",");
            final Node chainLink = propertyChain.jjtGetChild(i);
            final Node child = chainLink.jjtGetChild(0);
            if (chainLink instanceof ASTPropertyChainDot && child instanceof ASTIdentifier)
            {
                ctx.output(generator.quote(((ASTIdentifier) child).getName()));

            }
            else
            {
                ctx.applyRecursive(child);
            }
        }
    }


    private FunctionDefinition getActionDefinition(Node node)
    {
        if (node instanceof ASTFunction)
        {
            final String name = ((ASTFunction) node).getName();
            final Definition definition = definitions.getDefinition(name);

            if (definition instanceof FunctionDefinition)
            {
                final FunctionDefinition functionDefinition = (FunctionDefinition) definition;
                final ExpressionType restrictedTo = functionDefinition.getRestrictedTo();
                final boolean isAction = restrictedTo == ExpressionType.ACTION;
                if (isAction)
                {
                    if (serverside && functionDefinition.getDefinitionType() == DefinitionType.CLIENT_SIDE_ACTION)
                    {
                        throw new InvalidReferenceException(name + "() is only allowed in client-side action contexts");
                    }
                    return (FunctionDefinition) definition;
                }
            }
        }
        return null;
    }


    private void renderFork(ExpressionTransformationContext ctx, Node node)
    {
        ctx.output("Promise.all(");
        renderChildren(ctx, node);
        ctx.output(")");
    }

    private void renderCancel(ExpressionTransformationContext ctx, Node node)
    {
        renderChildren(ctx, node);
    }

    private void renderWhen(ExpressionTransformationContext ctx, Node node)
    {
        if (!(node.jjtGetParent() instanceof ASTPropertyChain))
        {
            throw new InvalidExpressionException("Ivalid when expression: " + ExpressionUtil.renderExpressionOf(node));
        }

        final Node kid = node.jjtGetChild(0);
        if (isAsync(kid))
        {
            transformActionOrSequence(ctx, kid);
        }
        else
        {

            ctx.output("Promise.resolve(");
            ctx.applyRecursive(kid);
            ctx.output(")");
        }
        if (!(node.jjtGetParent() instanceof ASTPropertyChain))
        {
            ctx.output(".then(_v.conditional)");
        }
    }


    private boolean isAsync(Node node)
    {
        if (getActionDefinition(node) != null)
        {
            return true;
        }

        for (int i=0; i < node.jjtGetNumChildren(); i++)
        {
            final boolean result = isAsync(node.jjtGetChild(i));
            if (result)
            {
                return true;
            }
        }
        return false;
    }


    private void renderChildren(ExpressionTransformationContext ctx, Node node)
    {
        final int numChildren = node.jjtGetNumChildren();
        for(int i = 0; i < numChildren; i++)
        {
            if (i > 0)
            {
                ctx.output(",");
            }

            final Node kid = node.jjtGetChild(i);
            transformActionOrSequence(ctx, kid);
        }
    }
}
