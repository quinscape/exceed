package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.Node;

/**
 * Test transform
 *
 * test(v) => _v.test({ value: v })
 *
 */
public class SpecialCaseTransformer
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
        ASTInteger astInteger = (ASTInteger)node;
        if (astInteger.getValue() == 1)
        {
            ctx.output("number1()");
        }
        else
        {
            ctx.renderDefault();
        }
    }
}
