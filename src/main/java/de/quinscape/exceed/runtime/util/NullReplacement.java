package de.quinscape.exceed.runtime.util;

import org.svenson.JSONable;

/**
 * Helper class to replace a null value in the function cache. JSONifies to the JSON value <code>null</code>.
 */
public final class NullReplacement implements JSONable
{
    private final static NullReplacement INSTANCE = new NullReplacement();

    private NullReplacement()
    {

    }

    public static Object wrap(Object value)
    {
        return value == null ? INSTANCE : value;
    }

    public static Object unwrap(Object value)
    {
        return value == INSTANCE ? null : value;
    }

    @Override
    public String toJSON()
    {
        return "null";
    }
}
