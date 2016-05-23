package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Operation;
import de.quinscape.exceed.runtime.scope.ScopedContext;

import java.util.Map;

@ExpressionOperations(environment = ContextExpressionEnvironment.class)
public class ContextExpressionOperations
{
    @Operation
    public Object param(ExpressionContext<ContextExpressionEnvironment> ctx, String name)
    {
        Map<String, Object> params = ctx.getEnv().getParams();

        if (params == null)
        {
            throw new IllegalStateException("Context has no parameters");
        }

        return params.get(name);
    }

    @Operation
    public Object processProperty(ExpressionContext<ContextExpressionEnvironment> ctx, String name)
    {
        ScopedContext scopedContext = ctx.getEnv().getScopedContext();
        if (scopedContext == null)
        {
            throw new IllegalStateException("Scoped context not initialized, can't access  process property");
        }

        return scopedContext.getProperty(name);
    }

    @Operation
    public Object processObject(ExpressionContext<ContextExpressionEnvironment> ctx, String name)
    {
        ScopedContext scopedContext = ctx.getEnv().getScopedContext();
        if (scopedContext == null)
        {
            throw new IllegalStateException("Scoped context not initialized, can't access  process object");
        }

        return scopedContext.getObject(name);
    }

    @Operation
    public Object processList(ExpressionContext<ContextExpressionEnvironment> ctx, String name)
    {
        ScopedContext scopedContext = ctx.getEnv().getScopedContext();
        if (scopedContext == null)
        {
            throw new IllegalStateException("Scoped context not initialized, can't access  process list");
        }

        return scopedContext.getList(name);
    }

}
