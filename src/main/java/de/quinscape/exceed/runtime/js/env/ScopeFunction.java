package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;

public class ScopeFunction
    extends AbstractConvertingFunction
{
    private final static Logger log = LoggerFactory.getLogger(ScopeFunction.class);

    public ScopeFunction(NashornScriptEngine nashorn, ScriptContext scriptContext, ApplicationModel applicationModel)
    {
        super(nashorn, scriptContext,applicationModel);
    }

    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        final RuntimeContext runtimeContext = RuntimeContextHolder.get();

        try
        {
            return getPath(runtimeContext, args[0]);
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException("Error setting context value", e);
        }

    }



}
