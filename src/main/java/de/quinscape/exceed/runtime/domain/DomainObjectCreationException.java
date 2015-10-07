package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class DomainObjectCreationException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -3631106417370656753L;

    public DomainObjectCreationException(String message)
    {
        super(message);
    }

    public DomainObjectCreationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DomainObjectCreationException(Throwable cause)
    {
        super(cause);
    }
}
