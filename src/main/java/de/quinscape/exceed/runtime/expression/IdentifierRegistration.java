package de.quinscape.exceed.runtime.expression;

import com.esotericsoftware.reflectasm.MethodAccess;

public final class IdentifierRegistration
{
    private final Object bean;

    private final MethodAccess access;

    private final int index;

    private final String desc;

    private final boolean takesEnvParam;


    public IdentifierRegistration(Object bean, MethodAccess access, int index, boolean takesEnvParam, String desc)
    {
        this.bean = bean;
        this.access = access;
        this.index = index;
        this.desc = desc;
        this.takesEnvParam = takesEnvParam;
    }


    public Object get(ExpressionEnvironment expressionEnvironment)
    {
        if (takesEnvParam)
        {
            return access.invoke(bean, index, expressionEnvironment);
        }
        else
        {
            return access.invoke(bean, index);
        }
    }
}
