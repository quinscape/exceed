package de.quinscape.exceed.runtime.service;

import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.editor.completion.expression.ChildRuleEnvironment;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Operation;

@ExpressionOperations(environment = ActionExecutionEnvironment.class)
public class ActionExecutionOperations
{
    @Operation
    public DomainObject object(ExpressionContext<ActionExecutionEnvironment> ctx, String name)
    {
        return ctx.getEnv().getScopedContext().getObject(name);
    }

    @Operation
    public DataList list(ExpressionContext<ActionExecutionEnvironment> ctx, String name)
    {
        return ctx.getEnv().getScopedContext().getList(name);
    }

    @Operation
    public Object property(ExpressionContext<ActionExecutionEnvironment> ctx, String name)
    {
        return ctx.getEnv().getScopedContext().getProperty(name);
    }
}
