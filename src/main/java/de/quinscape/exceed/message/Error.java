package de.quinscape.exceed.message;

public class Error
    extends Message
{
    private String message;

    /**
     * Create an error message to send to the client.
     *
     * @param errorMessage  error message
     */
    public Error(String errorMessage)
    {
        message = errorMessage;
    }


    public String getMessage()
    {
        return message;
    }
}
