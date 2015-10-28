package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.action.PongActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PongAction
    implements Action<PongActionModel, Integer>
{
    private static Logger log = LoggerFactory.getLogger(PongAction.class);

    @Override
    public Integer execute(RuntimeContext runtimeContext, PongActionModel model, Integer count)
    {
        return count + model.getIncrement();
    }


    @Override
    public Class<PongActionModel> getActionModelClass()
    {
        return PongActionModel.class;
    }


    @Override
    public Class<Integer> getInputClass()
    {
        return Integer.class;
    }
}
