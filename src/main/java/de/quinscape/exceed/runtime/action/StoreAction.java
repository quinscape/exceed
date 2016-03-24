package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.action.StoreActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreAction
    implements Action<StoreActionModel, DataList>
{
    private final static Logger log = LoggerFactory.getLogger(StoreAction.class);


    public StoreAction()
    {
        log.info("Create StoreAction");
    }


    @Override
    public Integer execute(RuntimeContext runtimeContext, StoreActionModel model, DataList input)
    {
        return 1;
    }


    @Override
    public Class<StoreActionModel> getActionModelClass()
    {
        return StoreActionModel.class;
    }


    @Override
    public Class<DataList> getInputClass()
    {
        return DataList.class;
    }
}
