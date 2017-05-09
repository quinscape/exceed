package de.quinscape.exceed.runtime.service.client;

/**
 * Defines the existing scopes for {@link ClientStateProvider} related data.
 * <p>
 * The {@link ClientStateService} caches the results according to the client state scope returned by the
 * {@link ClientStateProvider}.
 * </p>
 */
public enum ClientStateScope
{
    /**
     * Provider is evaluated for every request (default scope)
     */
    REQUEST,

    /**
     * Provider is evaluated once per view model.
     */
    VIEW,

    /**
     * Provider is evaluated once per user login. Provider will be skipped on AJAX requests.
     */
    USER,

    /**
     * Provider is evaluated once per application. Provider will be skipped on AJAX requests.
     */
    APPLICATION,

    /**
     * Provider is evaluated only once per exceed context startup.
     */
    CONSTANT,
    MODEL_VERSION;
}

