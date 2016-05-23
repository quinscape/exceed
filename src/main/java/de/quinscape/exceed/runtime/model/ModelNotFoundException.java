package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ModelNotFoundException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -8474337470611523408L;


    public ModelNotFoundException(String message)
    {
        super(message);
    }


    public ModelNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ModelNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
