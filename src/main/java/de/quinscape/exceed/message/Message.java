package de.quinscape.exceed.message;

import org.svenson.JSONProperty;

/**
 * Base class for all websocket message related classes. Provides the special "type" attribute.
 *
 * @see de.quinscape.exceed.runtime.model.ModelJSONServiceImpl.ModelMapper
 */
public abstract class Message
{
    public final static String MESSAGE_PREFIX = "message.";

    /**
     * Returns the type property for this message which starts with {@link #MESSAGE_PREFIX} ( "message.") followed
     * by the simple class name.
     *
     * @return type
     */
    @JSONProperty(readOnly = true)
    public final String getType()
    {
        return MESSAGE_PREFIX + this.getClass().getSimpleName();
    }
}
