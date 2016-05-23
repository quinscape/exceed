package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import org.svenson.util.JSONBeanUtil;

import java.util.Map;

/**
 * Environment for general context expressions.
 */
public class ContextExpressionEnvironment
    extends ExpressionEnvironment
{
    private final ActionService actionService;

    private final RuntimeContext runtimeContext;

    private final Map<String, Object> params;

    private final ScopedContext scopedContext;


    public ContextExpressionEnvironment(RuntimeContext runtimeContext, ActionService actionService, Map<String,
        Object> locationParams, ScopedContext scopedContext)
    {
        this.runtimeContext = runtimeContext;
        this.actionService = actionService;
        this.params = locationParams;
        this.scopedContext = scopedContext;
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
        return false;
    }


    public Map<String, Object> getParams()
    {
        return params;
    }

    public ScopedContext getScopedContext()
    {
        return scopedContext;
    }


    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }


    public ActionService getActionService()
    {
        return actionService;
    }
}
