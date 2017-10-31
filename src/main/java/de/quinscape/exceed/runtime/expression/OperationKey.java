package de.quinscape.exceed.runtime.expression;

import java.util.Objects;

public final class OperationKey
{

    private final Class<? extends ExpressionEnvironment> envClass;

    private final Class<?> context;

    private final String name;


    public OperationKey(Class<? extends ExpressionEnvironment> envClass, Class<?> context, String name)
    {
        if (envClass == null)
        {
            throw new IllegalArgumentException("envClass can't be null");
        }

        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        this.envClass = envClass;
        this.context = context;
        this.name = name;
    }


    public Class<? extends ExpressionEnvironment> getEnvClass()
    {
        return envClass;
    }


    public Class<?> getContext()
    {
        return context;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj instanceof OperationKey)
        {
            OperationKey that = (OperationKey) obj;
            return
                this.name.equals(that.name) &&
                this.envClass.getName().equals(that.envClass.getName()) &&
                Objects.equals(this.context, that.context);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return (envClass.hashCode() * 37 + ( context != null ? context.hashCode() * 17 : 19) + name.hashCode());
    }


    public String getName()
    {
        return name;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "envClass = " + envClass
            + ", context = " + context
            + ", name = '" + name + '\''
            ;
    }
}
