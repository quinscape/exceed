package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.Node;

/**
 * Implemented by classes customizing the rendering of expressions
 */
public interface ExpressionTransformer
{
    /**
     * Returns <code>true</code> if the transformer applies to the given node in the given context
     * @param ctx       context
     * @param node      node
     * @return
     */
    boolean appliesTo(ExpressionTransformationContext ctx, Node node);

    /**
     * Applies the transformer to the given node.
     *
     * @param ctx       context
     * @param node      node that {@link #appliesTo(ExpressionTransformationContext, Node)} returned <code>true</code> for
     */
    void apply(ExpressionTransformationContext ctx, Node node);
}
