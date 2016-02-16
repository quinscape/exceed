package de.quinscape.exceed.runtime.service.websocket;

import de.quinscape.exceed.message.Query;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.util.AppAuthentication;

/**
 * Provides access to context information for typed websocket messages.
 */
public interface MessageContext
{
    String getConnectionId();

    Object getSessionAttribute(String name);

    void setSessionAttribute(String name, Object value);

    /**
     * The app authentication of the user for which this message was generated.
     *
     * @return
     */
    AppAuthentication getLogin();

    /**
     * Reply to the given query with the given data. The data must be
     * convertable to JSON.
     *
     * @param query         query message
     * @param response      response data
     */
    void reply(Query query, Object response);

    /**
     * The runtime application in which this message takes place.
     *
     * @return
     */
    RuntimeApplication getRuntimeApplication();
}
