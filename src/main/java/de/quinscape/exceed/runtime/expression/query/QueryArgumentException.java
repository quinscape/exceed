package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class QueryArgumentException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 1878834991711351403L;


    public QueryArgumentException(String message)
    {
        super(message);
    }


    public QueryArgumentException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public QueryArgumentException(Throwable cause)
    {
        super(cause);
    }
}
