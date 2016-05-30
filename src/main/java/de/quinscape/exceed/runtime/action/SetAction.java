package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.action.SetActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedValueType;
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
    public void execute(RuntimeContext runtimeContext, SetActionModel model)
    {
        final ScopedValueType type = model.getType();
        final ScopedContextChain chain = runtimeContext.getScopedContextChain();

        final String name = model.getName();
        final String path = model.getPath();
        final Object value = model.getValue();

        final ScopedContext scope = type.findScope(chain, name);
        if (path == null)
        {
            type.set(scope, name, value);
        }
        else
        {
            util.setPropertyPath(type.get(scope,name), path, value);
        }
    }

    @Override
    public Class<SetActionModel> getActionModelClass()
    {
        return SetActionModel.class;
    }
}
