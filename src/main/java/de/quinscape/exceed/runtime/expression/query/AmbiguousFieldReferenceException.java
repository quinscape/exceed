package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class AmbiguousFieldReferenceException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -7782422746408680787L;


    public AmbiguousFieldReferenceException(String message)
    {
        super(message);
    }


    public AmbiguousFieldReferenceException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public AmbiguousFieldReferenceException(Throwable cause)
    {
        super(cause);
    }
}
