package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.expression.Operation;

import java.util.Set;

public class ParentRuleEnvironment
    extends ExpressionEnvironment
{
    private final Set<String> parentClasses;

    public ParentRuleEnvironment(Set<String> parentClasses)
    {
        this.parentClasses = parentClasses;

        logicalOperatorsAllowed = true;
        comparatorsAllowed = true;
    }

    @Operation
    public Boolean parentHasClass(ASTFunction node)
    {
        String cls = (String) node.jjtGetChild(0).jjtAccept(this, null);
        return parentClasses.contains(cls);
    }
}
