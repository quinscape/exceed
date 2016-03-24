package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.Operation;

@ExpressionOperations(environment = ChildRuleEnvironment.class)
public class ChildRuleOperations
{
    @Operation
    public Boolean component(ExpressionContext<ChildRuleEnvironment> ctx, String componentName)
    {
        return ctx.getEnv().getComponentName().equals(componentName);
    }

    @Operation
    public Boolean hasClass(ExpressionContext<ChildRuleEnvironment> ctx, String cls)
    {
        return ctx.getEnv().getComponentDescriptor().getClasses().contains(cls);
    }


}
