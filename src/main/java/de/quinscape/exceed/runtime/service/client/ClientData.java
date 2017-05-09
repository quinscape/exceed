package de.quinscape.exceed.runtime.service.client;

/**
 * Encapsulates the result of a {@link ClientStateProvider} invocation.
 *
 * @see ClientStateProvider
 */
public interface ClientData
{
    /**
     * Returns the JSON data for this client state result.
     * 
     * @return
     */
    String getJSON();
}
