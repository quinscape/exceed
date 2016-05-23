package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.model.InvalidClientExpressionException;

/**
 * Action implementation functioning as placeholder for client-side only action generators.
 *
 * They are known to the action service and can be rendered as view model expression but cannot be used within
 * server-side action execution.
 */
public class ClientSideOnlyAction
    implements Action<ClientSideOnlyActionModel>, ClientActionRenderer
{
    @Override
    public void execute(RuntimeContext runtimeContext, ClientSideOnlyActionModel model) throws Exception
    {
        throw new IllegalStateException("Action '" + model.getAction() + "' is a client-side only action and thus invalid in this context");
    }


    @Override
    public void renderJsCode(ExpressionRenderer renderer, ASTFunction node)
    {
        if (node.jjtGetNumChildren() != 1 && node.jjtGetNumChildren() != 2)
        {
            throw new InvalidClientExpressionException("navigateTo() must have one or two parameters (location, [params])");
        }
        StringBuilder buf = renderer.getBuffer();
        buf.append("_v.navigateTo(");

        node.jjtGetChild(0).jjtAccept(renderer, null);

        if (node.jjtGetNumChildren() == 2)
        {
            buf.append(", ");
            node.jjtGetChild(1).jjtAccept(renderer, null);
        }

        buf.append(")");
    }


    @Override
    public Class<ClientSideOnlyActionModel> getActionModelClass()
    {
        return ClientSideOnlyActionModel.class;
    }
}
