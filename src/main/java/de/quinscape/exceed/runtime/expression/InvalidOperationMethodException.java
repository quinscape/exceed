package de.quinscape.exceed.runtime.expression;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class InvalidOperationMethodException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 345421628475380185L;


    public InvalidOperationMethodException(String message)
    {
        super(message);
    }


    public InvalidOperationMethodException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidOperationMethodException(Throwable cause)
    {
        super(cause);
    }
}
