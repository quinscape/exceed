package de.quinscape.exceed.component;

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
    FILTER_EXPRESSION,
    ACTION_EXPRESSION
}
