package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;

public class VoidConverter
    implements PropertyConverter<Object,Object,Object>
{
    @Override
    public Object convertToJava(RuntimeContext runtimeContext, Object value)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object convertToJSON(RuntimeContext runtimeContext, Object value)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object convertToJs(RuntimeContext runtimeContext, Object value)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object convertFromJs(RuntimeContext runtimeContext, Object value)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public Class<Object> getJavaType()
    {
        return Object.class;
    }


    @Override
    public Class<Object> getJSONType()
    {
        return Object.class;
    }
}
