package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.Operation;

@ExpressionOperations(environment = ParentRuleEnvironment.class)
public class ParentRuleOperations
{
    @Operation
    public Boolean parentHasClass(ExpressionContext<ParentRuleEnvironment> ctx, String cls)
    {
        return ctx.getEnv().getParentClasses().contains(cls);
    }
}
