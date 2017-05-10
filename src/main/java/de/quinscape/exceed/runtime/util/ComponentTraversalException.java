package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ComponentTraversalException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -945362585127896978L;


    public ComponentTraversalException(String message)
    {
        super(message);
    }


    public ComponentTraversalException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ComponentTraversalException(Throwable cause)
    {
        super(cause);
    }
}
