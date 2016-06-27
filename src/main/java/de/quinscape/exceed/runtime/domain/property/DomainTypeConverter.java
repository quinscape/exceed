package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObjectBase;
import de.quinscape.exceed.runtime.util.DomainUtil;

public class DomainTypeConverter
    implements PropertyConverter<DomainObjectBase, DomainObjectBase>
{

    @Override
    public DomainObjectBase convertToJava(RuntimeContext runtimeContext, DomainObjectBase value) throws ParseException
    {
        if (value == null)
        {
            return null;
        }
        return (DomainObjectBase) DomainUtil.convertToJava(runtimeContext, value);
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
    public Class<DomainObjectBase> getJSONType()
    {
        return DomainObjectBase.class;
    }
}
