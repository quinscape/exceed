package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;

/**
 * Base class for neutral converters, converters that don't actually need to convert anything because the
 * values as they're parsed from JSON are the same expected on the Server side and vice versa.
 *
 * @param <T> type for both JSON and Java
 */
public abstract class NeutralConverter<T>
    implements PropertyConverter<T,T>
{
    private final Class<T> type;

    public NeutralConverter(Class<T> type)
    {
        this.type = type;
    }

    @Override
    public T convertToJava(RuntimeContext runtimeContext, T value)
    {
        return value;
    }

    @Override
    public T convertToJSON(RuntimeContext runtimeContext, T value)
    {
        return value;
    }

    @Override
    public final Class<T> getJavaType()
    {
        return getType();
    }

    @Override
    public final Class<T> getJSONType()
    {
        return getType();
    }

    public Class<T> getType()
    {
        return type;
    }
}
