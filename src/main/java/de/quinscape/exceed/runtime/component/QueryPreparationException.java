package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class QueryPreparationException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -8536392363806315124L;


    public QueryPreparationException(String message)
    {
        super(message);
    }


    public QueryPreparationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public QueryPreparationException(Throwable cause)
    {
        super(cause);
    }
}
