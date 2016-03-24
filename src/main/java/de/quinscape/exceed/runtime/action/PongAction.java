package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.action.PongActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PongAction
    implements Action<PongActionModel>
{
    private final static Logger log = LoggerFactory.getLogger(PongAction.class);


    @Override
    public void execute(RuntimeContext runtimeContext, PongActionModel model)
    {
    }


    @Override
    public Class<PongActionModel> getActionModelClass()
    {
        return PongActionModel.class;
    }

}
