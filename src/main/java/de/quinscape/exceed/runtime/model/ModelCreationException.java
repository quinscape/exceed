package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ModelCreationException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -1928532531014410518L;

    public ModelCreationException(String message)
    {
        super(message);
    }

    public ModelCreationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModelCreationException(Throwable cause)
    {
        super(cause);
    }
}
