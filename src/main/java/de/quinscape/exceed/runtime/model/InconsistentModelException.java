package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

/**
 * Thrown when an inconsistency within a model is found.
 *
 */
public class InconsistentModelException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 6593470700423879706L;


    public InconsistentModelException(String message)
    {
        super(message);
    }


    public InconsistentModelException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InconsistentModelException(Throwable cause)
    {
        super(cause);
    }
}
