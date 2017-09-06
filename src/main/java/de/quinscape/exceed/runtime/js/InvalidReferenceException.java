package de.quinscape.exceed.runtime.js;

public class InvalidReferenceException
    extends InvalidExpressionException
{
    private static final long serialVersionUID = 981966522009688295L;

    public InvalidReferenceException(String message)
    {
        super(message);
    }


    public InvalidReferenceException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidReferenceException(Throwable cause)
    {
        super(cause);
    }
}
