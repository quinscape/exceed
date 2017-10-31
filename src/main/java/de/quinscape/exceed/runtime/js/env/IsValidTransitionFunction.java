package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.StateMachine;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptContext;

public class IsValidTransitionFunction
    extends AbstractJSObject
{
    private final NashornScriptEngine nashorn;

    private final ScriptContext scriptContext;

    private final ApplicationModel applicationModel;


    public IsValidTransitionFunction(
        NashornScriptEngine nashorn, ScriptContext scriptContext, ApplicationModel applicationModel
    )
    {
        this.nashorn = nashorn;
        this.scriptContext = scriptContext;
        this.applicationModel = applicationModel;
    }


    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        String stateMachineName = (String) args[0];

        final StateMachine stateMachine = applicationModel.getStateMachines().get(stateMachineName);
        if (stateMachine == null)
        {
            throw new ExceedRuntimeException("State machine '" + stateMachineName + "' not found");
        }

        return stateMachine.isValidTransition((String)args[1], (String)args[2]);
    }

}
