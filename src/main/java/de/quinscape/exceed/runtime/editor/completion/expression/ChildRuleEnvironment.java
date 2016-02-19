package de.quinscape.exceed.runtime.editor.completion.expression;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;

public class ChildRuleEnvironment
    extends ExpressionEnvironment
{
    private final String componentName;

    private final ComponentDescriptor componentDescriptor;


    public ChildRuleEnvironment(String componentName, ComponentDescriptor componentDescriptor)
    {
        logicalOperatorsAllowed = true;
        comparatorsAllowed = true;

        this.componentName = componentName;
        this.componentDescriptor = componentDescriptor;
    }

    public Boolean component(ASTFunction node)
    {
        String componentName = (String) node.jjtGetChild(0).jjtAccept(this, null);
        return this.componentName.equals(componentName);
    }

    public Boolean hasClass(ASTFunction node)
    {
        String cls = (String) node.jjtGetChild(0).jjtAccept(this, null);
        return componentDescriptor.getClasses().contains(cls);
    }


}
