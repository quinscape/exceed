package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;

public abstract class NullConverter<T>
    implements PropertyConverter<T,T>
{
    private final Class<T> type;

    protected NullConverter(Class<T> type)
    {
        this.type = type;
    }

    @Override
    public T convertToJava(RuntimeContext runtimeContext, T value, DomainProperty param)
    {
        return value;
    }

    @Override
    public T convertToJSON(RuntimeContext runtimeContext, T value, DomainProperty property)
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
