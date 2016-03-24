package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;

public class ChildRuleEnvironment
    extends ExpressionEnvironment
{
    private final String componentName;

    private final ComponentDescriptor componentDescriptor;


    public ChildRuleEnvironment( String componentName, ComponentDescriptor componentDescriptor)
    {
        this.componentName = componentName;
        this.componentDescriptor = componentDescriptor;
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


    public ComponentDescriptor getComponentDescriptor()
    {
        return componentDescriptor;
    }


    public String getComponentName()
    {
        return componentName;
    }
}
