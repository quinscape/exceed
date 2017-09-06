package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.Node;

/**
 * Test transform
 *
 * test(v) => _v.test({ value: v })
 *
 */
public class NumberWrapper
    implements ExpressionTransformer
{
    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        return node instanceof ASTInteger;
    }

    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        ctx.output("num(" + ((ASTInteger)node).getValue() + ")");
    }
}
