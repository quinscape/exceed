package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class ExpressionCompilationException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -4544466548533713309L;


    public ExpressionCompilationException(String message)
    {
        super(message);
    }


    public ExpressionCompilationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ExpressionCompilationException(Throwable cause)
    {
        super(cause);
    }
}
