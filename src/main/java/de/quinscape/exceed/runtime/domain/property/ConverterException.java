package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ConverterException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = 5310238670223497350L;

    public ConverterException(String message)
    {
        super(message);
    }

    public ConverterException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ConverterException(Throwable cause)
    {
        super(cause);
    }
}
