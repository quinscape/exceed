package de.quinscape.exceed.runtime;

/**
 * Generic runtime exception for exceed.
 */
public class ExceedRuntimeException
    extends RuntimeException
{
    private static final long serialVersionUID = 8118130833748668602L;

    public ExceedRuntimeException(String message)
    {
        super(message);
    }

    public ExceedRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ExceedRuntimeException(Throwable cause)
    {
        super(cause);
    }
}
