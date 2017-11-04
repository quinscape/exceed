package de.quinscape.exceed.runtime.security;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ExceedSecurityException
    extends ExceedRuntimeException
{

    private static final long serialVersionUID = -4191899247146135267L;


    public ExceedSecurityException(String message)
    {
        super(message);
    }


    public ExceedSecurityException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ExceedSecurityException(Throwable cause)
    {
        super(cause);
    }
}
