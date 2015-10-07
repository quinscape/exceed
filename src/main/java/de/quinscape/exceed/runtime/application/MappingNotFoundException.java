package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class MappingNotFoundException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 1257882925667280946L;

    public MappingNotFoundException(String message)
    {
        super(message);
    }

    public MappingNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MappingNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
