package de.quinscape.exceed.message;

/**
 * Base class for all query messages. Query messages are incoming messages from the client to the server that can be
 * replied to, that is a client promise is waiting for us to provide it with data.
 *
 * @see de.quinscape.exceed.runtime.service.websocket.MessageContext#reply(Query, Object)
 */
public class Query
    extends IncomingMessage
{
    /**
     * Constructs an query message that can be replied to.
     *
     * @param meta meta block
     * @throws IllegalArgumentException if the meta is <code>null</code>
     */
    protected Query(MessageMeta meta)
    {
        super(meta);
    }
}
