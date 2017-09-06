package de.quinscape.exceed.runtime.js.def;

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
     * @see de.quinscape.exceed.model.domain.type.EnumType
     */
    ENUM,

    /**
     * Identifier is a state machine map.
     *
     * @see de.quinscape.exceed.model.state.StateMachine
     */
    STATE_MACHINE,

    /**
     * Function is a client-side only function. will be disallowed in server-side action contexts.
     *
     * @see CustomLogic
     * @see de.quinscape.exceed.runtime.action.Action
     */
    CLIENT_SIDE_ACTION
}
