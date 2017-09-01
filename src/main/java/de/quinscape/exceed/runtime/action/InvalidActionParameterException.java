package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class InvalidActionParameterException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -3549857618868846754L;


    public InvalidActionParameterException(String message)
    {
        super(message);
    }


    public InvalidActionParameterException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidActionParameterException(Throwable cause)
    {
        super(cause);
    }
}
