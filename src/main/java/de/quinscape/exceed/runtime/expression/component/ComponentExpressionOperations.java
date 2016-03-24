package de.quinscape.exceed.runtime.expression.component;

import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Operation;

@ExpressionOperations(environment = ComponentExpressionEnvironment.class)
public class ComponentExpressionOperations
{
    @Operation
    public Object prop(ExpressionContext<ComponentExpressionEnvironment> ctx)
    {
        ComponentExpressionEnvironment env = ctx.getEnv();
        StringBuilder output = env.getOutput();
        output.append("props[");
        ctx.getASTFunction().jjtGetChild(0).jjtAccept(env, null);
        output.append(']');
        return null;
    }


    @Operation
    public Object var(ExpressionContext<ComponentExpressionEnvironment> ctx)
    {
        ComponentExpressionEnvironment env = ctx.getEnv();
        StringBuilder output = env.getOutput();
        output.append("vars[");
        ctx.getASTFunction().jjtGetChild(0).jjtAccept(env, null);
        output.append(']');
        return null;
    }

}
