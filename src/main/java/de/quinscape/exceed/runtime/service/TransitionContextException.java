package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class TransitionContextException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 5020525044873011184L;


    public TransitionContextException(String message)
    {
        super(message);
    }


    public TransitionContextException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public TransitionContextException(Throwable cause)
    {
        super(cause);
    }
}
