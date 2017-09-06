package de.quinscape.exceed.runtime.scope;

/**
 * The existing scope types.
 */
public enum ScopeType
{
    /**
     * View context defined via <ViewContext/> component in either view JSON or layout JSON.
     *
     * Only valid within a non-process view / process view-state.
     */
    VIEW,

    /**
     * Process context defined process model. Valid during process execution. The only versioned scope. Each process
     * state change will produce a new copy of the process scope. If the user uses browser back and forward they can
     * jump between different process states and the corresponding versions of the process scope.
     */
    PROCESS,

    /**
     * Per-User Session context defined by the application model's "sessionContext" property.
     */
    SESSION,

    /**
     * Application context defined by the application model's "applicationContext" property. Contains general application configuration.
     */
    APPLICATION,

    /**
     * Transition level context containing context parameters for transitions.
     */
    TRANSITION,

    /**
     * Prototypic base scope for layout properties. Gets mixed into the the scope definitions for the views that use the layout.
     */
    LAYOUT
}
