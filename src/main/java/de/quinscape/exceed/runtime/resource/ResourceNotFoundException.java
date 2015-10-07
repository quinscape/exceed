package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ResourceNotFoundException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 2697666630927828057L;

    public ResourceNotFoundException(String message)
    {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ResourceNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
