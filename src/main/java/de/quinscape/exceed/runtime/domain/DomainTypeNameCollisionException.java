package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class DomainTypeNameCollisionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 3822571478071279144L;

    public DomainTypeNameCollisionException(String message)
    {
        super(message);
    }

    public DomainTypeNameCollisionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DomainTypeNameCollisionException(Throwable cause)
    {
        super(cause);
    }
}
