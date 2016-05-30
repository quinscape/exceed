package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;

public class IntegerConverter
    implements PropertyConverter<Integer, Long>
{

    @Override
    public Integer convertToJava(RuntimeContext runtimeContext, Long value)
    {
        return value != null ? value.intValue() : null;
    }

    @Override
    public Long convertToJSON(RuntimeContext runtimeContext, Integer value)
    {
        return value != null ? value.longValue() : null;
    }

    @Override
    public Class<Integer> getJavaType()
    {
        return Integer.class;
    }

    @Override
    public Class<Long> getJSONType()
    {
        return Long.class;
    }

}
