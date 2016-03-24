package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.action.PongActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PongAction
    implements Action<PongActionModel, PongData>
{
    private final static Logger log = LoggerFactory.getLogger(PongAction.class);

    @Override
    public PongData execute(RuntimeContext runtimeContext, PongActionModel model, PongData pongData)
    {
        return new PongData(pongData.getValue() + model.getIncrement());
    }


    @Override
    public Class<PongActionModel> getActionModelClass()
    {
        return PongActionModel.class;
    }


    @Override
    public Class<PongData> getInputClass()
    {
        return PongData.class;
    }
}
