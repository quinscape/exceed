package de.quinscape.exceed.tooling;

import java.lang.reflect.Method;

public class InfoMethods
{
    private final Method getter;

    private final Method setter;


    public InfoMethods(Method getter, Method setter)
    {

        this.getter = getter;
        this.setter = setter;
    }


    public Method getGetter()
    {
        return getter;
    }


    public Method getSetter()
    {
        return setter;
    }
}
