package de.quinscape.exceed.runtime.js;

/**
 * The different types of expressions in an exceed application
 */
public enum ExpressionType
{
    /**
     * Expression that produces a value
     */
    VALUE,
    /**
     * Asynchronous action (sequence) expression
     */
    ACTION,
    /**
     * A rule expression that checks a given object, basically a predicate for that object type.
     */
    RULE,
    /**
     * Query expression, gets transformed into data layer queries.
     */
    QUERY,
    /**
     * A filter expression
     */
    FILTER,

    CONTEXT, /**
     * A cursor expression that points to a data location within a larger context
     */
    CURSOR
}
