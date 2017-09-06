package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ConversionFailureException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -3418526703478501474L;


    public ConversionFailureException(String message)
    {
        super(message);
    }


    public ConversionFailureException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ConversionFailureException(Throwable cause)
    {
        super(cause);
    }
}
