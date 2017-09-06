package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTAdd;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;

/**
 *  Test transform
 *
 *  a + b + c => add('extra', a, b, c)
 *
 */
public class PlusTransform
    implements ExpressionTransformer
{
    private final String superArg;


    public PlusTransform(String superArg)
    {
        this.superArg = superArg;
    }


    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        return node instanceof ASTAdd;
    }


    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        ctx.output("add(");
        ctx.output(SingleQuoteJSONGenerator.INSTANCE.forValue(superArg));
        ctx.output(", ");
        ctx.renderMultiBinary(node, ", ");
        ctx.output(")");
    }
}
