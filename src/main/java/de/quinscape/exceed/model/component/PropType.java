package de.quinscape.exceed.model.component;

/**
 * Enumerates all the different prop types an exceed component attribute can have.
 */
public enum PropType
{
    PLAINTEXT,

    INTEGER,

    FLOAT,

    BOOLEAN,
    
    MAP,

    /** space separated set of classes */
    CLASSES,

    /**
     * Glyph icon name
     */
    GLYPH_ICON,

    /** transition name */
    TRANSITION,

    /**
     * Property reference a field for a query.
     */
    FIELD_REFERENCE,

    /**
     * Field references a domain type.
     */
    DOMAIN_TYPE_REFERENCE,

    STATE_MACHINE_REFERENCE,

    /** Query expression transformed into a JOOQ query */
    QUERY_EXPRESSION,

    /**
     * Expression providing a data cursor from a surrounding context
     */
    CURSOR_EXPRESSION,

    /**
     * Standalone filter expression to be embedded in a query expression
     */
    FILTER_EXPRESSION,

    /**
     * Normal value providing expression
     */
    VALUE_EXPRESSION,

    /**
     * Promise-backed
     */
    ACTION_EXPRESSION,

    /**
     * Expression providing a value from the surrounding context, wrapped in a function to be repeatedly invocable.
     */
    CONTEXT_EXPRESSION
}
