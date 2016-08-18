package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.ComponentPropWizard;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.editor.completion.AceCompletion;
import de.quinscape.exceed.runtime.editor.completion.CompletionType;
import de.quinscape.exceed.runtime.editor.completion.PropWizard;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;

import java.util.List;

public class PropCompleteEnvironment
    extends ExpressionEnvironment
{
    private final String propName;

    private final View viewModel;

    private final ComponentModel componentModel;

    private final QueryTransformer queryTransformer;

    private final RuntimeContext runtimeContext;


    public PropCompleteEnvironment(
        RuntimeContext runtimeContext,
        QueryTransformer queryTransformer,
        View viewModel,
        ComponentModel componentModel,
        String propName)
    {
        this.propName = propName;
        this.viewModel = viewModel;
        this.componentModel = componentModel;
        this.queryTransformer = queryTransformer;
        this.runtimeContext = runtimeContext;
    }


    public String getPropName()
    {
        return propName;
    }


    public View getViewModel()
    {
        return viewModel;
    }


    public ComponentModel getComponentModel()
    {
        return componentModel;
    }


    public QueryTransformer getQueryTransformer()
    {
        return queryTransformer;
    }


    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }


    @Override
    protected boolean logicalOperatorsAllowed()
    {
        return false;
    }


    @Override
    protected boolean comparatorsAllowed()
    {
        return false;
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


    @Override
    protected boolean expressionSequenceAllowed()
    {
        return false;
    }


    public List<AceCompletion> evaluate(ExpressionService expressionService)
    {
        ComponentDescriptor descriptor = componentModel.getComponentRegistration().getDescriptor();
        ASTExpression ruleExpression = descriptor.getPropTypes().get(propName).getRuleExpression();

        Object o = expressionService.evaluate(ruleExpression, this);

        if (o instanceof List)
        {
            List<AceCompletion> list = (List<AceCompletion>) o;
            if (list.size() == 0 || list.get(0) != null)
            {
                ComponentPropWizard componentPropWizard = descriptor.getComponentPropWizards().get(propName);
                if (componentPropWizard != null)
                {

                    list.add(new AceCompletion(CompletionType.PROP, "...", componentPropWizard.getTitle(), componentPropWizard.getDescription(), null, new PropWizard(propName)));
                }

                return list;
            }
        }

        throw new IllegalStateException(ExpressionRenderer.render(ruleExpression) + " produced no list of prop suggestions");
    }

}
