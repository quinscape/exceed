package de.quinscape.exceed.runtime.expression;

public class UnknownOperationException
    extends ExpressionEnvironmentException
{
    private static final long serialVersionUID = 5383314027027576661L;


    public UnknownOperationException(String message)
    {
        super(message);
    }


    public UnknownOperationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public UnknownOperationException(Throwable cause)
    {
        super(cause);
    }
}
