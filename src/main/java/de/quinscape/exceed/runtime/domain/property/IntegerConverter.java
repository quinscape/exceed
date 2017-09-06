package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;

public class IntegerConverter
    implements PropertyConverter<Integer, Long, Number>
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


    @Override
    public Number convertToJs(RuntimeContext
                                  runtimeContext, Integer value)
    {
        if (value == null)
        {
            return null;
        }
        return Double.valueOf(value);
    }


    @Override
    public Integer convertFromJs(RuntimeContext
                                     runtimeContext, Number value)
    {
        if (value == null)
        {
            return null;
        }

        return value.intValue();
    }
}
