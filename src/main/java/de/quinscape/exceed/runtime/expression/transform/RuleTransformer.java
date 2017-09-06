package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.domain.DomainRule;

import java.util.Map;

public class RuleTransformer
    implements ExpressionTransformer
{

    private final Map<String, DomainRule> domainRules;


    public RuleTransformer(Map<String, DomainRule> domainRules)
    {
        this.domainRules = domainRules;
    }


    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        return (node instanceof ASTFunction) && domainRules.containsKey(((ASTFunction) node).getName());
    }


    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        ASTFunction fn = (ASTFunction)node;
        final DomainRule domainRule = domainRules.get(fn.getName());

        final ASTExpression astExpression = domainRule.getRuleValue().getAstExpression();
        final String identifier = astExpression.annotation().getIdentifier();
        if (identifier == null)
        {
            throw new IllegalStateException("CompilationResult not provided for " + astExpression);
        }

        ctx.output(identifier);
        ctx.output("(");
        ctx.renderMultiBinary(node, ",");
        ctx.output(")");
    }
}
