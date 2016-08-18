package de.quinscape.exceed.model.component;

/**
 * Enumerates all the different prop types available to exceed components
 */
public enum PropType
{
    PLAINTEXT,
    INTEGER,
    FLOAT,
    BOOLEAN,

    /** space separated set of classes */
    CLASSES,
    /** transition name */
    TRANSITION,

    QUERY_EXPRESSION,
    CURSOR_EXPRESSION,
    FILTER_EXPRESSION,
    VALUE_EXPRESSION,
    ACTION_EXPRESSION
}
