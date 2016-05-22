package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTExpressionSequence;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.service.ActionExpressionRenderer;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public abstract class ActionExpressionBaseRenderer
    extends ExpressionRenderer
{
    protected final ActionExpressionRenderer actionExpressionRenderer;

    public ActionExpressionBaseRenderer(ActionExpressionRenderer actionExpressionRenderer)
    {
        this.actionExpressionRenderer = actionExpressionRenderer;
    }

    @Override
    public Object visit(ASTFunction node, Object data)
    {
        String operationName = node.getName();
        if (handleLocalFunctions(operationName, node))
        {
            // already handled
            return data;
        }
        else
        {
            if (actionExpressionRenderer != null && actionExpressionRenderer.hasOperation(operationName))
            {
                actionExpressionRenderer.render(operationName, node, this);
                return data;
            }
            // render as-is
            return super.visit(node, data);
        }
    }

    @Override
    public Object visit(ASTPropertyChain node, Object data)
    {
        Node firstChild = node.jjtGetChild(0);
        if (firstChild instanceof ASTIdentifier)
        {
            if (!handleLocalIdentifiers(node, (ASTIdentifier) firstChild))
            {
                return super.visit(node, data);
            }
        }
        else if (firstChild instanceof ASTFunction)
        {
            if (actionExpressionRenderer != null)
            {
                return super.visit(node, data);
            }
            else
            {
                return super.visit(node, data);
            }
        }
        return data;
    }


    @Override
    public Object visit(ASTExpressionSequence node, Object data)
    {
        if (ExpressionUtil.isChainOfActions(node, actionExpressionRenderer))
        {
            // yes -> delegate
            actionExpressionRenderer.renderChain(node, this);
            return data;
        }
        return super.visit(node, data);
    }


    protected abstract boolean handleLocalIdentifiers(ASTPropertyChain node, ASTIdentifier identifier);

    protected abstract boolean handleLocalFunctions(String operationName, ASTFunction node);
}
