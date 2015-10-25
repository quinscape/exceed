package de.quinscape.exceed.runtime.expression;

public class UnknownContextException
    extends ExpressionEnvironmentException
{
    private static final long serialVersionUID = -7142806918719026402L;


    public UnknownContextException(String message)
    {
        super(message);
    }


    public UnknownContextException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public UnknownContextException(Throwable cause)
    {
        super(cause);
    }
}
