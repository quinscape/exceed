package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTMapEntry;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.action.ServerActionConverter;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.controller.ActionExecutionException;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.expression.annotation.Operation;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.scope.ScopedResolver;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.svenson.util.JSONBeanUtil;

/**
 * Environment for the defition and execution of server-side actions within a transtion action list
 */
public class ActionExecutionEnvironment
    extends ExpressionEnvironment
{
    private final ScopedResolver scopedContext;

    private final ActionService actionService;

    private final String processName;

    private final RuntimeContext runtimeContext;


    public ActionExecutionEnvironment(RuntimeContext runtimeContext, ScopedResolver
        scopedContext, ActionService actionService, String processName)
    {
        this.runtimeContext = runtimeContext;
        this.scopedContext = scopedContext;
        this.actionService = actionService;
        this.processName = processName;
    }


    @Override
    protected boolean logicalOperatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean comparatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean complexLiteralsAllowed()
    {
        return true;
    }


    @Override
    protected boolean arithmeticOperatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean expressionSequenceAllowed()
    {
        return true;
    }


    @Override
    public Object undefinedOperation(ExpressionContext<ExpressionEnvironment> ctx, ASTFunction node, Object chainObject)
    {
        try
        {
            Action action = actionService.getAction(node.getName());

            Class cls = action.getActionModelClass();

            JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

            ActionModel actionModel;
            if (action instanceof ServerActionConverter)
            {
                actionModel = ((ServerActionConverter) action).createModel(ctx, node);
            }
            else
            {
                boolean isMap = node.jjtGetNumChildren() == 1 && node.jjtGetChild(0) instanceof ASTMap;
                if (node.jjtGetNumChildren() != 0 && !isMap)
                {
                    throw new IllegalStateException("Invalid arguments for unconverted operation '" + node.getName()
                        + "': Args must be empty or one map literal.");
                }
                actionModel = (ActionModel) cls.newInstance();

                if (isMap)
                {
                    ASTMap map = (ASTMap) node.jjtGetChild(0);

                    for (int i = 0; i < map.jjtGetNumChildren(); i++)
                    {
                        ASTMapEntry e = (ASTMapEntry) map.jjtGetChild(i);

                        Node key = e.jjtGetChild(0);
                        String name;
                        if (key instanceof ASTIdentifier)
                        {
                            name = ((ASTIdentifier) key).getName();
                        }
                        else
                        {
                            name = ((ASTString)key).getValue();
                        }
                        Object value = e.jjtGetChild(1).jjtAccept(this, null);

                        util.setProperty(actionModel, name, value);
                    }
                }
            }

            // It certainly is the class the action declared in getActionModelClass
            //noinspection unchecked
            action.execute(runtimeContext, actionModel);
            return true;
        }
        catch (Exception e)
        {
            throw new ActionExecutionException("Error executing operation " + ExpressionRenderer.render(node), e);
        }
    }


    public ActionService getActionService()
    {
        return actionService;
    }


    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }


    public ScopedResolver getScopedContext()
    {
        return scopedContext;
    }


    @Override
    public Object resolveIdentifier(String name)
    {
        if (scopedContext.hasProperty(name))
        {
            return scopedContext.getProperty(name);
        }
        else
        {
            return super.resolveIdentifier(name);
        }
    }
}
