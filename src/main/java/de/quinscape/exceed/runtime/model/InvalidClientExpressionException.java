package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class InvalidClientExpressionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 8803059350836514046L;


    public InvalidClientExpressionException(String message)
    {
        super(message);
    }


    public InvalidClientExpressionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidClientExpressionException(Throwable cause)
    {
        super(cause);
    }
}
