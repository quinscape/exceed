package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ProcessExecutionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -3981019379785638523L;


    public ProcessExecutionException(String message)
    {
        super(message);
    }


    public ProcessExecutionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ProcessExecutionException(Throwable cause)
    {
        super(cause);
    }
}
