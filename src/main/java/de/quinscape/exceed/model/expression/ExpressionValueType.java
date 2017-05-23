package de.quinscape.exceed.model.expression;

/**
 * Encapsulates the type of expression values.
 */
public enum ExpressionValueType
{
    /**
     * Expression value is a string literal.
     *
     */
    STRING,
    /**
     * Expression value is an expression.
     */
    EXPRESSION,
    /**
     * Expression failed to parse. Use {@link ExpressionValue#getExpressionError()} to get the exception that occurred.
     *
     */
    EXPRESSION_ERROR
}
