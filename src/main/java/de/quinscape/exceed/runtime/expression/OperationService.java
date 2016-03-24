package de.quinscape.exceed.runtime.expression;

import de.quinscape.exceed.expression.ASTFunction;

/**
 * Internal interface used by the expression environments to access the registered opererations and identifiers.
 */
interface OperationService
{
    /**
     * Returns the identifier for the given expression.
     * <p>
     *     If none of the defined operations method define an identifier with the given name, the implementation will
     *     fall back to {@link ExpressionEnvironment#resolveIdentifier(String)}.
     * </p>
     *
     * @param env       expression environment
     * @param name      identifier name
     *
     * @return  identifier value
     */
    Object resolveIdentifier(ExpressionEnvironment env, String name);

    /**
     * Evaluates the operation expressed by the given AST function in the given expression environment
     * and with the given context
     *
     * @param expressionEnvironment     expression environment
     * @param node                      AST operation function
     * @param context                   context object to apply the operation to or <code>null</code>
     *
     * @return evaluation result
     */
    Object evaluate(ExpressionEnvironment expressionEnvironment, ASTFunction node, Object context);

}
