package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;

/**
 * Context provided to {@link ExpressionTransformer#apply(ExpressionTransformationContext, Node)}.
 */
public interface ExpressionTransformationContext
{
    /**
     * Applies the current expression transformation recursively onto the given child node.
     *
     * @param node      child node
     */
    void applyRecursive(Node node);

    /**
     * Output the given Javascript snippet.
     *
     * @param s     Javascript snippet
     */
    void output(Object s);

    /**
     * Returns the collected output of the expression transformation context.
     *
     * @return  output
     */
    String getOutput();

    /**
     * Renders the children of the node as multiple binary operations with the given operand.
     *
     * @param node      node whose child nodes are rendered
     * @param op        Javascript operand string
     */
    void renderMultiBinary(Node node, String op);

    void clearRenderDefault();
    
    void renderDefault();

    ExpressionType getExpressionType();

    ExpressionModelContext getModelContext();

    ApplicationModel getApplicationModel();
    
    /**
     * Pushes the given constant value code up the scope chain and returns an identifier for that value.
     *
     * <p>
     *      Allows a transformer or definition renderer to define complex constant values that are only evaluated once per
     *      server bundle or view.
     * </p>
     *
     * @param identifierBase    identifier base name
     * @param code              Javascript value code
     *
     * @return unique identifier starting with base name
     */
    String pushCodeBlock(String identifierBase, String code);

}
