package de.quinscape.exceed.runtime.controller;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ActionNotFoundException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -8407563360065197357L;


    public ActionNotFoundException(String message)
    {
        super(message);
    }


    public ActionNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ActionNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
