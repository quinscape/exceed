package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.runtime.ExceedRuntimeException;

public class AssignmentReplacementException
    extends ExceedRuntimeException
{
    private static final long serialVersionUID = -4647767577413018750L;


    public AssignmentReplacementException(String message)
    {
        super(message);
    }


    public AssignmentReplacementException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public AssignmentReplacementException(Throwable cause)
    {
        super(cause);
    }
}
