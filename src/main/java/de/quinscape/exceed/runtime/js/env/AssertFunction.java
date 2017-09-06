package de.quinscape.exceed.runtime.js.env;

import jdk.nashorn.api.scripting.AbstractJSObject;

class AssertFunction
    extends AbstractJSObject
{
    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        boolean isOk = false;

        if (args.length > 0)
        {
            final Object arg = args[0];
            if (arg instanceof Boolean)
            {
                if (((Boolean) arg).booleanValue())
                {
                    return null;
                }
            }
            else if (arg instanceof String)
            {
                if (((String) arg).length() > 0)
                {
                    return null;
                }
            }
            else if (arg instanceof Number)
            {
                if (((Number) arg).intValue() != 0)
                {
                    return null;
                }
            }
        }

        throw new AssertionError();
    }
}
