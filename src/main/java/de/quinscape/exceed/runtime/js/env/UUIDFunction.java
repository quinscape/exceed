package de.quinscape.exceed.runtime.js.env;

import jdk.nashorn.api.scripting.AbstractJSObject;

import java.util.UUID;

public final class UUIDFunction
    extends AbstractJSObject
{
    public final static UUIDFunction INSTANCE = new UUIDFunction();

    private UUIDFunction()
    {

    }

    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        return UUID.randomUUID().toString();
    }
}
