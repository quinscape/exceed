package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.domain.StateMachine;
import de.quinscape.exceed.runtime.expression.transform.StateMachineValueTransformer;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public class StateMachineTypeResolver
    implements PropertyTypeResolver
{
    private final StateMachine stateMachine;


    public StateMachineTypeResolver(StateMachine stateMachine)
    {
        this.stateMachine = stateMachine;
    }


    @Override
    public PropertyModel resolve(
        TypeAnalyzerContext context, ResolvableNode node, ExpressionModelContext contextModel
    )
    {
        Node parent = node.jjtGetParent();
        if (parent instanceof ASTPropertyChain)
        {
            final Node second = parent.jjtGetChild(1).jjtGetChild(0);
            if (second instanceof ASTIdentifier)
            {
                if (node.jjtGetNumChildren() == 3)
                {
                    final Node third = node.jjtGetChild(2).jjtGetChild(0);
                    if (third instanceof ASTFunction && ((ASTFunction) third).getName().equals(
                        StateMachineValueTransformer.FROM))
                    {
                        return ExpressionUtil.BOOLEAN_TYPE;
                    }
                    return null;
                }
                return DomainProperty.builder()
                    .withType(PropertyType.MAP, stateMachine.getName())
                    .build();
            }
            throw new InvalidExpressionException("Invalid State machine expression: " + ExpressionUtil.renderExpressionOf(second));
        }
        return DomainProperty.builder()
            .withType(PropertyType.MAP, stateMachine.getName())
            .build();
    }
}
