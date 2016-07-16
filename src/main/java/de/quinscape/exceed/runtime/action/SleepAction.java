package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.runtime.RuntimeContext;

public class SleepAction
    implements Action<SleepActionModel>
{
    @Override
    public Object execute(RuntimeContext runtimeContext, SleepActionModel model) throws Exception
    {
        Thread.sleep(model.getTime());
        return true;
    }


    @Override
    public Class<SleepActionModel> getActionModelClass()
    {
        return SleepActionModel.class;
    }
}
