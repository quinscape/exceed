package de.quinscape.exceed.runtime.js.def;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.expression.transform.ExpressionTransformationContext;

public interface DefinitionRenderer
{
    void render(ExpressionTransformationContext ctx, Node node);
}
