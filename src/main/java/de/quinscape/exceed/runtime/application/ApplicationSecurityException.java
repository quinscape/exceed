package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ApplicationSecurityException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 486559400210368254L;


    public ApplicationSecurityException(String message)
    {
        super(message);
    }


    public ApplicationSecurityException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ApplicationSecurityException(Throwable cause)
    {
        super(cause);
    }
}
