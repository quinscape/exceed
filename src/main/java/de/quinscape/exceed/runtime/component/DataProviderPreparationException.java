package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class DataProviderPreparationException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 4152963296696015103L;

    private final String id;


    public DataProviderPreparationException(String id, String message)
    {
        super(message);
        this.id = id;
    }


    public DataProviderPreparationException(String id, String message, Throwable cause)
    {
        super(message, cause);
        this.id = id;
    }


    public String getId()
    {
        return id;
    }
}
