package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ScopedElementNotFoundException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -4207487852853922987L;


    public ScopedElementNotFoundException(String message)
    {
        super(message);
    }


    public ScopedElementNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ScopedElementNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
