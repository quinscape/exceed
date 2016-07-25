package de.quinscape.exceed.message;

/**
 * Direct response to an incoming client message
 *
 * The incoming message must have been initiated with hub.request(data) on the client side.
 *
 */
public class Reply
    extends Message
{
    private final int responseId;

    private final boolean ok;

    private final Object message;
    
    public Reply(int responseId, Object applicationMessage)
    {
        this(responseId, applicationMessage, true);
    }

    private  Reply(int responseId, Object applicationMessage, boolean ok)
    {
        this.responseId = responseId;
        message = applicationMessage;
        this.ok = ok;
    }


    /**
     * Returns the reply payload. The client handler will only receive this
     * object as JSON data.
     *
     * @return
     */
    public Object getMessage()
    {
        return message;
    }


    /**
     * Returns the response id for this reply which must be the same message id as the original
     * {@link de.quinscape.exceed.message.IncomingMessage} the reply is replying to.
     * @return
     */
    public Integer getResponseId()
    {
        return responseId;
    }


    public boolean isOk()
    {
        return ok;
    }


    /**
     * Creates a reply that signals failure for the corresponding request.
     *
     * Use this when you nothing do to handle an exceptional failure for your request. No client handler will
     * see this response, it will just be used internall to clean up and reject the corresponding promise.
     *
     * @param responseId    Response id
     * @param message       error message
     *
     * @return error reply
     */
    public static Reply createErrorReply(int responseId, Object message)
    {
        return new Reply(responseId, message, false);
    }
}
