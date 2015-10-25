package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class DataProviderPreparationException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 4152963296696015103L;


    public DataProviderPreparationException(String message)
    {
        super(message);
    }


    public DataProviderPreparationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public DataProviderPreparationException(Throwable cause)
    {
        super(cause);
    }
}
