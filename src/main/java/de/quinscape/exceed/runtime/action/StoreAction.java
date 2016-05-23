package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.model.action.StoreActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.model.InvalidClientExpressionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class StoreAction
    implements Action<StoreActionModel>, ClientActionRenderer
{
    private final static Logger log = LoggerFactory.getLogger(StoreAction.class);

    public StoreAction()
    {
        log.info("Create StoreAction");
    }

    @Override
    public void execute(RuntimeContext runtimeContext, StoreActionModel model)
    {
        GenericDomainObject domainObject = model.getData();
        domainObject.update();
    }

    @Override
    public Class<StoreActionModel> getActionModelClass()
    {
        return StoreActionModel.class;
    }


    @Override
    public void renderJsCode(ExpressionRenderer renderer, ASTFunction node)
    {
        if (node.jjtGetNumChildren() != 1 && node.jjtGetNumChildren() != 2)
        {
            throw new InvalidClientExpressionException("store() needs one or two parameters ( data , [ŧype])");
        }

        StringBuilder buf = renderer.getBuffer();
        buf.append("_a.action({ action: \"store\", data: ");

        node.jjtGetChild(0).jjtAccept(renderer, null);

        if (node.jjtGetNumChildren() == 2)
        {
            buf.append(",");
            node.jjtGetChild(1).jjtAccept(renderer, null);
        }
        else
        {
            buf.append("})");
        }


    }
}
