package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class VariableResolutionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 9031680952480058924L;


    public VariableResolutionException(String message)
    {
        super(message);
    }


    public VariableResolutionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public VariableResolutionException(Throwable cause)
    {
        super(cause);
    }
}
