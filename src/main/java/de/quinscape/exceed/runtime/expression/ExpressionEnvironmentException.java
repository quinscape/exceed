package de.quinscape.exceed.runtime.expression;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ExpressionEnvironmentException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 1994670375052942456L;


    public ExpressionEnvironmentException(String message)
    {
        super(message);
    }


    public ExpressionEnvironmentException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ExpressionEnvironmentException(Throwable cause)
    {
        super(cause);
    }
}
