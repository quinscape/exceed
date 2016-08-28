package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainObjectBase;
import de.quinscape.exceed.runtime.util.DomainUtil;

import java.util.Map;

public class DomainTypeConverter
    implements PropertyConverter<DomainObjectBase, Object>
{

    @Override
    public DomainObjectBase convertToJava(RuntimeContext runtimeContext, Object value) throws ParseException
    {
        if (value == null)
        {
            return null;
        }

        if (value instanceof DomainObjectBase)
        {
            return (DomainObjectBase) DomainUtil.convertToJava(runtimeContext, (DomainObject) value);
        }
        else if (value instanceof Map)
        {
            return (DomainObjectBase) DomainUtil.convertToJava(runtimeContext, (Map<String,Object>)value);
        }
        else
        {
            throw new IllegalStateException("Invalid domain object value" + value);
        }
    }


    @Override
    public DomainObjectBase convertToJSON(RuntimeContext runtimeContext, DomainObjectBase value)
    {
        if (value == null)
        {
            return null;
        }
        return (DomainObjectBase) DomainUtil.convertToJSON(runtimeContext, value);
    }


    @Override
    public Class<DomainObjectBase> getJavaType()
    {
        return DomainObjectBase.class;
    }


    @Override
    public Class<Object> getJSONType()
    {
        return Object.class;
    }
}
