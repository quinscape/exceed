package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.state.StateMachine;
import de.quinscape.exceed.runtime.RuntimeContext;

public class StateConverter
    extends PlainTextConverter
{
    private final StateMachine stateMachine;


    public StateConverter(StateMachine stateMachine)
    {
        this.stateMachine = stateMachine;
    }


    @Override
    public String convertToJava(RuntimeContext runtimeContext, String value)
    {
        ensureValid(value);
        return super.convertToJava(runtimeContext, value);
    }


    @Override
    public String convertToJSON(RuntimeContext runtimeContext, String value)
    {
        ensureValid(value);
        return super.convertToJSON(runtimeContext, value);
    }


    @Override
    public String convertToJs(RuntimeContext runtimeContext, String value)
    {
        ensureValid(value);
        return super.convertToJs(runtimeContext, value);
    }


    @Override
    public String convertFromJs(RuntimeContext runtimeContext, String value)
    {
        ensureValid(value);
        return super.convertFromJs(runtimeContext, value);
    }


    private void ensureValid(String value)
    {
        if (!stateMachine.getStates().containsKey(value))
        {
            throw new IllegalStateException(value + " is not a valid state in " + stateMachine);
        }
    }
}
