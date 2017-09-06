package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.expression.transform.ExpressionTransformationContext;
import de.quinscape.exceed.runtime.js.def.DefinitionRenderer;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.util.JSONUtil;

public class DebugFunctionRenderer
    implements DefinitionRenderer
{
    @Override
    public void render(ExpressionTransformationContext ctx, Node node)
    {
        ctx.output("_v.debug([");

        final int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++)
        {
            final Node kid = node.jjtGetChild(i);
            if (i > 0)
            {
                ctx.output(", ");
            }
            ctx.output(JSONUtil.DEFAULT_GENERATOR.quote(ExpressionRenderer.render(kid)));
            ctx.output(", ");
            ctx.applyRecursive(kid);
        }
        ctx.output("])");
    }
}
