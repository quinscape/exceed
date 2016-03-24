package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ActionExecutionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -1445612697500894869L;

    public ActionExecutionException(String message)
    {
        super(message);
    }


    public ActionExecutionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ActionExecutionException(Throwable cause)
    {
        super(cause);
    }
}
