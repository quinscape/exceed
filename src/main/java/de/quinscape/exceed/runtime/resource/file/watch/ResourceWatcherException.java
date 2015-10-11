package de.quinscape.exceed.runtime.resource.file.watch;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

import java.io.IOException;

public class ResourceWatcherException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 2929095696856678268L;


    public ResourceWatcherException(String message)
    {
        super(message);
    }


    public ResourceWatcherException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ResourceWatcherException(Throwable cause)
    {
        super(cause);
    }
}
