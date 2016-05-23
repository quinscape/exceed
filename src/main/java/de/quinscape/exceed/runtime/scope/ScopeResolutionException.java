package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ScopeResolutionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -501762994362149632L;


    public ScopeResolutionException(String message)
    {
        super(message);
    }


    public ScopeResolutionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ScopeResolutionException(Throwable cause)
    {
        super(cause);
    }
}
