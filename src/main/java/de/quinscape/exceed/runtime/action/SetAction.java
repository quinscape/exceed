package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.model.action.SetActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.model.InvalidClientExpressionException;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.service.ActionExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class SetAction
    implements Action<SetActionModel>
{
    private final static Logger log = LoggerFactory.getLogger(SetAction.class);

    public SetAction()
    {
        log.info("Create StoreAction");
    }

    @Override
    public void execute(RuntimeContext runtimeContext, SetActionModel model)
    {
        ScopedContext scope = runtimeContext.getScopedContextChain().findScopeWithProperty(model.getName());

        log.debug("SetProperty '{}' to {}", model.getName(), model.getValue());

        scope.setProperty(model.getName(), model.getValue());
    }

    @Override
    public Class<SetActionModel> getActionModelClass()
    {
        return SetActionModel.class;
    }
}
