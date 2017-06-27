package de.quinscape.exceed.model.translation;

/**
 * Represents the type of source for the translation tag references.
 */
public enum ReferenceType
{
    /** Translation was referenced by a module */
    MODULE,

    /** Translation key comes from domain type and field names */
    DOMAIN,

    /**
     * Translation key is the name of a language itself.
     */
    LOCALE,

    /**
     * Translation key is referenced inside a view model, i.e. in a view model attribute expression.
     */
    VIEW,

    /**
     * Translation key is the unqualified variant of a translation key with qualifier.
     */
    QUALIFIER
}
