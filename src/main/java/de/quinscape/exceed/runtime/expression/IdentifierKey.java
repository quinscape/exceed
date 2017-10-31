package de.quinscape.exceed.runtime.expression;

public final class IdentifierKey
{
    private final Class<? extends ExpressionEnvironment> envClass;

    private final String name;


    IdentifierKey(Class<? extends ExpressionEnvironment> envClass, String name)
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
        this.name = name;
    }

    public Class<? extends ExpressionEnvironment> getEnvClass()
    {
        return envClass;
    }


    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj instanceof IdentifierKey)
        {
            IdentifierKey that = (IdentifierKey) obj;
            return this.envClass.equals(that.envClass) && this.name.equals(that.name);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return (envClass.hashCode() * 37) + name.hashCode();
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "envClass = " + envClass
            + ", name = '" + name + '\''
            ;
    }
}
