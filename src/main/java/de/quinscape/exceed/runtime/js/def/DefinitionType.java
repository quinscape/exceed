package de.quinscape.exceed.runtime.js.def;

import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.StateMachine;
import de.quinscape.exceed.runtime.action.CustomLogic;

/**
 * Classifies identifier and function definitions according to their source
 */
public enum DefinitionType
{
    /**
     * Builtin identifier or function
     */
    BUILTIN,

    /**
     * Identifier is backed by one of the contexts (view, process, session, or application)
     */
    CONTEXT,

    /**
     * Identifier is a rule function.
     *
     * @see de.quinscape.exceed.model.domain.DomainRule
     */
    RULE,

    /**
     * Function is a server-side action function.
     *
     * @see CustomLogic
     * @see de.quinscape.exceed.runtime.action.Action
     */
    ACTION,

    /**
     * Identifier is a enum type map.
     *
     * @see EnumType
     */
    ENUM,

    /**
     * Identifier is a state machine map.
     *
     * @see StateMachine
     */
    STATE_MACHINE,

    /**
     * Operation is a query expression operation. Used for documentation purposes only.
     */
    QUERY,

    /**
     * Operation is a filter expression operation. Used for documentation purposes only.
     */
    QUERY_FILTER,

    /**
     * Function is a client-side only function. will be disallowed in server-side action contexts.
     *
     * @see CustomLogic
     * @see de.quinscape.exceed.runtime.action.Action
     */
    CLIENT_SIDE_ACTION
}
