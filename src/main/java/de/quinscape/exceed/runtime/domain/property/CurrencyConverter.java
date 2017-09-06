package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;

public class CurrencyConverter
    implements PropertyConverter<Long, Long, Double>
{
    private final String currency;

    public CurrencyConverter(String currency)
    {
        this.currency = currency;
    }


    @Override
    public Long convertToJava(RuntimeContext runtimeContext, Long value)
    {
        return value;
    }


    @Override
    public Long convertToJSON(RuntimeContext runtimeContext, Long value)
    {
        return value;
    }


    @Override
    public Class<Long> getJavaType()
    {
        return Long.class;
    }


    @Override
    public Class<Long> getJSONType()
    {
        return Long.class;
    }


    @Override
    public Double convertToJs(RuntimeContext runtimeContext, Long value)
    {
        if (value == null)
        {
            return null;
        }
        return value.doubleValue();
    }


    @Override
    public Long convertFromJs(RuntimeContext runtimeContext, Double value)
    {
        if (value == null)
        {
            return null;
        }
        return value.longValue();
    }
}
