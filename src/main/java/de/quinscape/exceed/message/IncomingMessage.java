package de.quinscape.exceed.message;

import org.svenson.JSONProperty;

/**
 * Base class for incoming messages.
 *
 * Provides the meta block for routing and processing purposes.
 *
 */
public abstract class IncomingMessage
    extends Message
{
    private final MessageMeta meta;


    /**
     * Constructs an incoming message
     *
     * @param meta  meta block
     * @throws IllegalArgumentException if the meta is <code>null</code>
     */
    protected IncomingMessage(MessageMeta meta)
    {
        if (meta == null)
        {
            throw new IllegalArgumentException("meta can't be null");
        }
        this.meta = meta;
    }


    /**
     * Returns the meta block for this message. Mostly used for incoming messages.
     * @return
     */
    @JSONProperty(ignoreIfNull = true, priority = -1)
    public final MessageMeta getMeta()
    {
        return meta;
    }


}
