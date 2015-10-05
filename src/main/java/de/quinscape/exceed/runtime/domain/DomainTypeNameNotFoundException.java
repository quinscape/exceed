package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class DomainTypeNameNotFoundException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -4816465325249896789L;

    public DomainTypeNameNotFoundException(String message)
    {
        super(message);
    }

    public DomainTypeNameNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DomainTypeNameNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
