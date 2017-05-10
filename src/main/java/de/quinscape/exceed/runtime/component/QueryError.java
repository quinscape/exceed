package de.quinscape.exceed.runtime.component;

import org.svenson.JSONProperty;

public class QueryError
{
    private final String queryName;

    private final String message;

    private final Throwable throwable;


    public QueryError(String queryName, String message, Throwable throwable)
    {
        this.queryName = queryName;
        this.throwable = throwable;
        this.message = message + ": " + throwable.getMessage();
    }

    public String getMessage()
    {
        return message;
    }

    @JSONProperty(ignore = true)
    public Throwable getThrowable()
    {
        return throwable;
    }


    public String getType()
    {
        return throwable.getClass().getSimpleName();
    }


    public String getQueryName()
    {
        return queryName;
    }
}
