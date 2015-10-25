package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ModelCompositionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -5321140798389748836L;


    public ModelCompositionException(String message)
    {
        super(message);
    }


    public ModelCompositionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ModelCompositionException(Throwable cause)
    {
        super(cause);
    }
}
