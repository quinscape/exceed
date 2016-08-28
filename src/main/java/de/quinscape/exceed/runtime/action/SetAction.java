package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.action.SetActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.svenson.util.JSONPathUtil;

@Transactional
public class SetAction
    implements Action<SetActionModel>
{
    private final static Logger log = LoggerFactory.getLogger(SetAction.class);

    private JSONPathUtil util = new JSONPathUtil();

    public SetAction()
    {
        log.info("Create StoreAction");
    }

    @Override
    public Object execute(RuntimeContext runtimeContext, SetActionModel model)
    {
        final ScopedContextChain chain = runtimeContext.getScopedContextChain();

        final String name = model.getName();
        final String path = model.getPath();
        final Object value = model.getValue();

        if (path == null)
        {
            chain.setProperty(name, value);
        }
        else
        {
            util.setPropertyPath(chain.getProperty(name), path, value);
        }
        return true;
    }

    @Override
    public Class<SetActionModel> getActionModelClass()
    {
        return SetActionModel.class;
    }
}
