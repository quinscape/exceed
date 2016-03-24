package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;

import java.util.Set;

public class ParentRuleEnvironment
    extends ExpressionEnvironment
{
    private final RuntimeApplication runtimeApplication;

    private final View viewModel;

    private final Set<String> parentClasses;

    public ParentRuleEnvironment(RuntimeApplication runtimeApplication, View viewModel, Set<String> parentClasses)
    {
        this.runtimeApplication = runtimeApplication;
        this.viewModel = viewModel;
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


    public RuntimeApplication getRuntimeApplication()
    {
        return runtimeApplication;
    }


    public View getViewModel()
    {
        return viewModel;
    }
}
