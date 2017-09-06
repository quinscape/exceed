package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.Node;

public class EqualityTransformer
    implements ExpressionTransformer
{
    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        return node instanceof ASTEquality;
    }


    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        ASTEquality eq = (ASTEquality)node;

        ctx.applyRecursive(eq.jjtGetChild(0));

        // render == and != as === and !==
        ctx.output(" "  + eq.getOperator().getAsString() + "= ");

        ctx.applyRecursive(eq.jjtGetChild(1));
    }
}
