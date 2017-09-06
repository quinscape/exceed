package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.Node;

/**
 * Test transform
 *
 * test(v) => _v.test({ value: v })
 *
 */
public class TestTransform
    implements ExpressionTransformer
{
    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        return node instanceof ASTFunction && ((ASTFunction) node).getName().equals("test");
    }

    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        ctx.output("_v.test({ value: ");
        ctx.renderMultiBinary(node, ", ");
        ctx.output("})");

    }
}
