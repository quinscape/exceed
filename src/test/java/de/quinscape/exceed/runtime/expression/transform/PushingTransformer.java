package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.Node;

/**
 * Test transform
 *
 * <code>pushTrigger => pushTrigger(constant2)</code> ( with <code>var constant2 = 'PUSHED'</code> pushed)
 *
 */
public class PushingTransformer
    implements ExpressionTransformer
{
    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        return node instanceof ASTIdentifier;
    }

    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        ASTIdentifier ident = (ASTIdentifier) node;


        if (ident.getName().equals("pushTrigger"))
        {
            final String identifier = ctx.pushCodeBlock("constant", "'PUSHED'");

            ctx.output("pushTrigger(" + identifier + ")");
        }
    }

}
