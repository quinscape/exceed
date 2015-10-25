package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class QueryValidationException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -2443652519163397421L;


    public QueryValidationException(String message)
    {
        super(message);
    }


    public QueryValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public QueryValidationException(Throwable cause)
    {
        super(cause);
    }
}
