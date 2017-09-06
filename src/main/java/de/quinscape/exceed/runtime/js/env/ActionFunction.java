package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.action.ActionResult;
import de.quinscape.exceed.runtime.action.ActionService;
import de.quinscape.exceed.runtime.action.JSParameters;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;

public class ActionFunction
    extends AbstractJSObject
{
    private final ActionService actionService;


    public ActionFunction(ActionService actionService)
    {
        this.actionService = actionService;
    }


    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public JSObject call(Object thiz, Object... args)
    {
        final RuntimeContext runtimeContext = RuntimeContextHolder.get();

        try
        {
            final ActionResult result = actionService.execute(
                runtimeContext,
                (String) args[0],
                new JSParameters(
                    (JSObject)args[1]
                ));

            if (result.isResolved())
            {
                return Promise.resolve(runtimeContext, result.toJS());
            }
            else
            {
                return Promise.reject(runtimeContext, result.toJS());
            }
        }
        catch(Exception e)
        {
            return Promise.reject(runtimeContext, e);
        }
    }
}
