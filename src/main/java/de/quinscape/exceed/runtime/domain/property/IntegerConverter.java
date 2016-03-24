package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;

public class IntegerConverter
    implements PropertyConverter<Integer, Long>
{

    @Override
    public Integer convertToJava(RuntimeContext runtimeContext, Long value, DomainProperty param)
    {
        return value.intValue();
    }

    @Override
    public Long convertToJSON(RuntimeContext runtimeContext, Integer value, DomainProperty property)
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
