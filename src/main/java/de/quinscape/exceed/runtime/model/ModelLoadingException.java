package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ModelLoadingException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -1424481892533575952L;

    public ModelLoadingException(String message)
    {
        super(message);
    }

    public ModelLoadingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModelLoadingException(Throwable cause)
    {
        super(cause);
    }
}
