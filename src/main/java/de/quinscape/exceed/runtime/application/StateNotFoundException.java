package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class StateNotFoundException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -1769114372416971608L;


    public StateNotFoundException(String message)
    {
        super(message);
    }


    public StateNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public StateNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
