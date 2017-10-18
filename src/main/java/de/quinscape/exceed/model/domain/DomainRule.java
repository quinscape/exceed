package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.svenson.JSONProperty;

import javax.annotation.PostConstruct;

/**
 * Declares a reusable and composable domain rule. 
 *
 */
public class DomainRule
    extends AbstractTopLevelModel
{
    private DomainProperty target;

    private ExpressionValue rule;

    private ExpressionValue errorMessage;

    private String description;


    @Override
    public <I, O> O accept(TopLevelModelVisitor<I, O> visitor, I in)
    {
        return visitor.visit(this, in);
    }


    public String getRule()
    {
        return rule.getValue();
    }


    public void setRule(String rule)
    {
        this.rule = ExpressionValue.forValue(rule, true);
    }

    @JSONProperty(ignore = true)
    public ExpressionValue getRuleValue()
    {
        return rule;
    }


    public DomainProperty getTarget()
    {
        return target;
    }


    public void setTarget(DomainProperty target)
    {
        this.target = target;
    }


    public String getErrorMessage()
    {
        return errorMessage.getValue();
    }

    @JSONProperty(ignore = true)
    public ExpressionValue getErrorMessageValue()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = ExpressionValue.forValue(errorMessage, true);
    }

    @PostConstruct
    public void init()
    {
        if (rule == null)
        {
            throw new InconsistentModelException("Domain rule '" + getName() + "' has no rule expression.");
        }

        if (target == null)
        {
            throw new InconsistentModelException("Domain rule '" + getName() + "' has no target definition.");
        }

        if (this.errorMessage == null)
        {
            this.errorMessage =  ExpressionValue.forValue(

                "i18n('" + getName() + ":invalid')",
                
                true
            );
        }
    }


    public void postProcess(ApplicationModel applicationModel)
    {
        applicationModel.getMetaData().createPropertyType(target);
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = " + getName()
            + ", rule = " + ExpressionRenderer.render(rule.getAstExpression())
            ;
    }
}
