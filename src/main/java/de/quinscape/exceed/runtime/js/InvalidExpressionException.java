package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class InvalidExpressionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 3643543176745599350L;


    public InvalidExpressionException(String message)
    {
        super(message);
    }


    public InvalidExpressionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidExpressionException(Throwable cause)
    {
        super(cause);
    }
}
