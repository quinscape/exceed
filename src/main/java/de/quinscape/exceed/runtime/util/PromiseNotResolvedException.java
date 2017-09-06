package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class PromiseNotResolvedException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -3552963412565741469L;


    public PromiseNotResolvedException(String message)
    {
        super(message);
    }


    public PromiseNotResolvedException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public PromiseNotResolvedException(Throwable cause)
    {
        super(cause);
    }
}
