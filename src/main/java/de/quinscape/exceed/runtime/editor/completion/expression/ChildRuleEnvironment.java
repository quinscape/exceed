package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;

public class ChildRuleEnvironment
    extends ExpressionEnvironment
{
    private final RuntimeApplication runtimeApplication;

    private final View viewModel;

    private final String componentName;

    private final ComponentDescriptor componentDescriptor;


    public ChildRuleEnvironment(RuntimeApplication runtimeApplication, View viewModel, String componentName, ComponentDescriptor componentDescriptor)


    {
        this.runtimeApplication = runtimeApplication;
        this.viewModel = viewModel;
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


    public RuntimeApplication getRuntimeApplication()
    {
        return runtimeApplication;
    }


    public View getViewModel()
    {
        return viewModel;
    }
}
