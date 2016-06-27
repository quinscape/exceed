package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.model.context.ScopedElementModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.controller.ActionService;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;

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

    private final ScopedElementModel scopedElementModel;

    private final ScopedContextChain scopedContext;


    public ContextExpressionEnvironment(RuntimeContext runtimeContext, ActionService actionService, Map<String,
        Object> locationParams, ScopedElementModel scopedElementModel)
    {
        this.runtimeContext = runtimeContext;
        this.actionService = actionService;
        this.params = locationParams;
        this.scopedElementModel = scopedElementModel;
        this.scopedContext = runtimeContext.getScopedContextChain();
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

    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }


    public ActionService getActionService()
    {
        return actionService;
    }


    public ScopedElementModel getScopedElementModel()
    {
        return scopedElementModel;
    }
}
