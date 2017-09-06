package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.expression.transform.ExpressionTransformationContext;
import de.quinscape.exceed.runtime.js.def.DefinitionRenderer;

public class FunctionReplacementRenderer
    implements DefinitionRenderer
{
    private final String replacement;
    public FunctionReplacementRenderer(String replacement)
    {
        this.replacement = replacement;
    }

    @Override
    public void render(ExpressionTransformationContext ctx, Node node)
    {
        ctx.output(replacement);
        ctx.output("(");
        ctx.renderMultiBinary(node, ",");
        ctx.output(")");
    }
}
