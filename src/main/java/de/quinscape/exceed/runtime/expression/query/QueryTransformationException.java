package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class QueryTransformationException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 860784314374455934L;


    public QueryTransformationException(String message)
    {
        super(message);
    }


    public QueryTransformationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public QueryTransformationException(Throwable cause)
    {
        super(cause);
    }
}
