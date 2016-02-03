package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.runtime.RuntimeContext;

public class SleepAction
    implements Action<SleepActionModel, Object>
{
    @Override
    public Object execute(RuntimeContext runtimeContext, SleepActionModel model, Object input) throws Exception
    {
        Thread.sleep(model.getTime());
        return input;
    }


    @Override
    public Class<SleepActionModel> getActionModelClass()
    {
        return SleepActionModel.class;
    }


    @Override
    public Class<Object> getInputClass()
    {
        return Object.class;
    }
}
