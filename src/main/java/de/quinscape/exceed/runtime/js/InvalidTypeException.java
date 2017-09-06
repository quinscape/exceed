package de.quinscape.exceed.runtime.js;

public class InvalidTypeException
    extends InvalidExpressionException
{
    private static final long serialVersionUID = 1445080508364891871L;


    public InvalidTypeException(String message)
    {
        super(message);
    }


    public InvalidTypeException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidTypeException(Throwable cause)
    {
        super(cause);
    }
}
