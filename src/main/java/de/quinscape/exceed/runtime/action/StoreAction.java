package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.model.action.StoreActionModel;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainObjectBase;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.model.InvalidClientExpressionException;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.service.ActionExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class StoreAction
    implements Action<StoreActionModel>, ClientActionRenderer, ServerActionConverter<StoreActionModel>
{
    private final static Logger log = LoggerFactory.getLogger(StoreAction.class);

    public StoreAction()
    {
        log.info("Create StoreAction");
    }

    @Override
    public void execute(RuntimeContext runtimeContext, StoreActionModel model)
    {
        DomainObject domainObject = model.getObject();
        if (domainObject == null)
        {
            throw new IllegalStateException("No domain object in store action model");
        }
        domainObject.insertOrUpdate();
    }

    @Override
    public Class<StoreActionModel> getActionModelClass()
    {
        return StoreActionModel.class;
    }


    @Override
    public void renderJsCode(View view, ExpressionRenderer renderer, ASTFunction node)
    {
        if (node.jjtGetNumChildren() > 1)
        {
            throw new InvalidClientExpressionException("store() needs at most one parameter ( [object] )");
        }

        StringBuilder buf = renderer.getBuffer();
        buf.append("_a.action({ action: \"store\", object: ");

        if (node.jjtGetNumChildren() == 1)
        {
            node.jjtGetChild(0).jjtAccept(renderer, null);
        }
        else
        {
            if (view.isContainedInProcess())
            {
                buf.append("object('" + ProcessContext.DOMAIN_OBJECT_CONTEXT + "')");
            }
            else
            {
                buf.append("context");
            }
        }
        buf.append("})");

    }


    @Override
    public StoreActionModel createModel(ExpressionContext<ActionExecutionEnvironment> ctx, ASTFunction node)
    {
        if (node.jjtGetNumChildren() > 1)
        {
            throw new InvalidClientExpressionException("store() needs at most one parameter ( [object] )");
        }
        StoreActionModel storeActionModel = new StoreActionModel();

        DomainObject domainObject;
        if (node.jjtGetNumChildren() == 1)
        {
            Object o = node.jjtGetChild(0).jjtAccept(ctx.getEnv(), null);

            if (!(o instanceof DomainObject))
            {
                throw new IllegalStateException("Argument to store() did not resolve to domain object, but " + o);
            }
            domainObject = (DomainObject) o;
        }
        else
        {
            domainObject = ctx.getEnv().getScopedContext().getObject(ProcessContext.DOMAIN_OBJECT_CONTEXT);
        }
        domainObject.setDomainService(ctx.getEnv().getRuntimeContext().getDomainService());
        storeActionModel.setObject((DomainObjectBase) domainObject);
        return storeActionModel;
    }
}
