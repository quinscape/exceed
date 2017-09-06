package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionRenderer;
import de.quinscape.exceed.runtime.js.def.Definitions;

public class DefinitionTransformer
    implements ExpressionTransformer
{
    private final Definitions definitions;


    public DefinitionTransformer(Definitions definitions)
    {
        this.definitions = definitions;
    }


    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        return node instanceof ResolvableNode;
    }


    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        ResolvableNode resolvableNode = (ResolvableNode) node;

        final Definition definition = definitions.getDefinition(resolvableNode.getName());
        DefinitionRenderer renderer;
        if (definition != null && (renderer = definition.getDefinitionRenderer()) != null)
        {
            renderer.render(ctx, node);
        }
        else
        {
            ctx.renderDefault();
        }
    }
}
