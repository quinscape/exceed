package de.quinscape.exceed.message;

import de.quinscape.exceed.message.IncomingMessage;
import de.quinscape.exceed.message.MessageMeta;
import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.List;

/**
 * A group of messages that is being sent and processed as a batch.
 */
public class Group
    extends IncomingMessage
{
    private final List<IncomingMessage> messages;


    public Group(

        @JSONParameter("meta")
        MessageMeta meta,

        @JSONParameter("messages")
        @JSONTypeHint(IncomingMessage.class)
        List<IncomingMessage> messages
    )
    {
        super(meta);
        this.messages = messages;
    }


    public List<IncomingMessage> getMessages()
    {
        return messages;
    }
}
