package de.quinscape.exceed.message;

import org.svenson.JSONParameter;

/**
 * Message meta block needed for incoming messages for routing and responding purposes.
 */
public final class MessageMeta
{
    /**
     * Connection Id for the connection that sent the message
     */
    private final String connectionId;

    /**
     * Numeric message id
     */
    private final Integer messageId;

    private final String appName;


    public MessageMeta(
        @JSONParameter("connectionId")
        String connectionId,
        @JSONParameter("messageId")
        Integer messageId,
        @JSONParameter("appName")
        String appName)
    {
        this.connectionId = connectionId;
        this.messageId = messageId;
        this.appName = appName;
    }


    public String getConnectionId()
    {
        return connectionId;
    }


    public Integer getMessageId()
    {
        return messageId;
    }


    public String getAppName()
    {
        return appName;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "connectionId = '" + connectionId + '\''
            + ", messageId = " + messageId
            + ", appName = '" + appName + '\''
            ;
    }
}
