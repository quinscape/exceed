package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class InvalidMachineStateException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 2511941115805017798L;


    public InvalidMachineStateException(String message)
    {
        super(message);
    }


    public InvalidMachineStateException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidMachineStateException(Throwable cause)
    {
        super(cause);
    }
}
