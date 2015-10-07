package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class DomainTypeOverridingException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -5472449160453764392L;

    public DomainTypeOverridingException(String message)
    {
        super(message);
    }

    public DomainTypeOverridingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DomainTypeOverridingException(Throwable cause)
    {
        super(cause);
    }
}
