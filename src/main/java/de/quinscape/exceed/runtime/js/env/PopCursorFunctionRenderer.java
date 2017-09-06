package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.expression.transform.ExpressionTransformationContext;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.def.DefinitionRenderer;

public class PopCursorFunctionRenderer
    implements DefinitionRenderer
{
    @Override
    public void render(ExpressionTransformationContext ctx, Node node)
    {
        if (node.jjtGetNumChildren() == 0)
        {
            throw new InvalidExpressionException("popCursor(cursor[,howMany]) function needs at least one cursor argument.");
        }

        if (node.jjtGetNumChildren() > 2)
        {
            throw new InvalidExpressionException("popCursor(cursor[,howMany]) takes at most 2 parameters");
        }

        ctx.applyRecursive(node.jjtGetChild(0));

        if (node.jjtGetNumChildren() == 2)
        {
            ctx.output(".pop(");
            ctx.applyRecursive(node.jjtGetChild(1));
            ctx.output(")");
        }
        else
        {
            ctx.output(".pop()");
        }
    }
}
