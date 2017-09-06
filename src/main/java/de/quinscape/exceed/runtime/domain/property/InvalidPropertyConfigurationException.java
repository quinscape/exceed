package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class InvalidPropertyConfigurationException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -1126334522106695814L;


    public InvalidPropertyConfigurationException(String message)
    {
        super(message);
    }


    public InvalidPropertyConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidPropertyConfigurationException(Throwable cause)
    {
        super(cause);
    }
}
