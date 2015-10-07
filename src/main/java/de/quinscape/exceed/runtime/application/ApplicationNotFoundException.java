package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ApplicationNotFoundException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -1019327296160416369L;

    public ApplicationNotFoundException(String message)
    {
        super(message);
    }

    public ApplicationNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ApplicationNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
