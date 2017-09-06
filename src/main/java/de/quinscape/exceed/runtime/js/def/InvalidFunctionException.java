package de.quinscape.exceed.runtime.js.def;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class InvalidFunctionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 1175292321706578550L;


    public InvalidFunctionException(String message)
    {
        super(message);
    }


    public InvalidFunctionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidFunctionException(Throwable cause)
    {
        super(cause);
    }
}
