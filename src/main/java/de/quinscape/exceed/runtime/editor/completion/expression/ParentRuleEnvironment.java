package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;

import java.util.Set;

public class ParentRuleEnvironment
    extends ExpressionEnvironment
{
    private final Set<String> parentClasses;

    public ParentRuleEnvironment(Set<String> parentClasses)
    {
        this.parentClasses = parentClasses;

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
        return false;
    }


    @Override
    protected boolean arithmeticOperatorsAllowed()
    {
        return false;
    }


    public Set<String> getParentClasses()
    {
        return parentClasses;
    }
}
