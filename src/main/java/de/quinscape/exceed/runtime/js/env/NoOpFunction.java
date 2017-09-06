package de.quinscape.exceed.runtime.js.env;

import jdk.nashorn.api.scripting.AbstractJSObject;

public final class NoOpFunction
    extends AbstractJSObject
{
    private NoOpFunction()
    {
    }

    public final static NoOpFunction INSTANCE = new NoOpFunction();

    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        // intentionally left empty
        return null;
    }
}
