package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class InvalidFieldReferenceException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -345960963702920983L;


    public InvalidFieldReferenceException(String message)
    {
        super(message);
    }


    public InvalidFieldReferenceException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidFieldReferenceException(Throwable cause)
    {
        super(cause);
    }
}
