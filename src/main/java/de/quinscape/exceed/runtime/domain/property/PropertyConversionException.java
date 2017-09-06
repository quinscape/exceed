package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class PropertyConversionException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -1847855582416962773L;


    public PropertyConversionException(String message)
    {
        super(message);
    }


    public PropertyConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public PropertyConversionException(Throwable cause)
    {
        super(cause);
    }
}
