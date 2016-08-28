package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ScopeNameCollisionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -3119258885055370556L;


    public ScopeNameCollisionException(String message)
    {
        super(message);
    }


    public ScopeNameCollisionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ScopeNameCollisionException(Throwable cause)
    {
        super(cause);
    }
}
