package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Operation;

@ExpressionOperations(environment = ParentRuleEnvironment.class)
public class ParentRuleOperations
{
    @Operation
    public Boolean parentHasClass(ExpressionContext<ParentRuleEnvironment> ctx, String cls)
    {
        return ctx.getEnv().getParentClasses().contains(cls);
    }

    @Operation
    public Integer index(ExpressionContext<ParentRuleEnvironment> ctx)
    {
        return ctx.getEnv().getIndex();
    }

    @Operation
    public boolean viewInProcess(ExpressionContext<ParentRuleEnvironment> ctx)
    {
        ParentRuleEnvironment env = ctx.getEnv();
        return env.getViewModel().isContainedInProcess();
    }
}
