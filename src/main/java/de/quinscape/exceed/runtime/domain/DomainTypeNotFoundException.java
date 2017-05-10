package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class DomainTypeNotFoundException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -1857626289080239065L;


    public DomainTypeNotFoundException(String message)
    {
        super(message);
    }


    public DomainTypeNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public DomainTypeNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
