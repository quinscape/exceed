package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ServiceNotReadyException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 7873028646798326500L;


    public ServiceNotReadyException(String message)
    {
        super(message);
    }


    public ServiceNotReadyException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ServiceNotReadyException(Throwable cause)
    {
        super(cause);
    }
}
