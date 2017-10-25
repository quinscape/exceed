package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.state.StateMachine;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;

import java.util.Map;

public class StateMachineValueTransformer
    implements ExpressionTransformer
{

    public static final String FROM = "from";

    private final Map<String, StateMachine> stateMachines;


    public StateMachineValueTransformer(ApplicationModel applicationModel)
    {
        this.stateMachines = applicationModel.getStateMachines();
    }

    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        if (node instanceof ASTPropertyChain)
        {
            final Node kid = node.jjtGetChild(0);

            if (kid instanceof ASTIdentifier)
            {
                final String name = ((ASTIdentifier) kid).getName();
                if (stateMachines.containsKey(name))
                {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        final SingleQuoteJSONGenerator generator = SingleQuoteJSONGenerator.INSTANCE;

        final String stateMachineName = ((ASTIdentifier) node.jjtGetChild(0)).getName();

        final Node second = node.jjtGetChild(1).jjtGetChild(0);

        final StateMachine stateMachine = stateMachines.get(stateMachineName);
        if (second instanceof ASTIdentifier)
        {
            final String value = ((ASTIdentifier) second).getName();

            if (node.jjtGetNumChildren() == 3)
            {
                final Node third = node.jjtGetChild(2).jjtGetChild(0);
                if (third instanceof ASTFunction && ((ASTFunction) third).getName().equals(FROM) && third.jjtGetNumChildren() == 1)
                {
                    ctx.output("_v.isValidTransition(" + generator.quote(stateMachineName) + ", " );

                    final PropertyModel annotatedType = third.annotation().getPropertyType();

                    if ( annotatedType != null && (
                            !annotatedType.getType().equals("State") ||
                            !annotatedType.getTypeParam().equals(stateMachineName)
                    ))
                    {
                        throw new InvalidExpressionException("from(): Argument is not a state machine state of tyoe '" + stateMachineName + "'");
                    }

                    ctx.applyRecursive(third.jjtGetChild(0));

                    ctx.output( ", " + generator.quote(value)+ ")");
                }
            }
            else
            {
                if (value.equals(StateMachine.START))
                {
                    ctx.output(generator.quote(stateMachine.getStartState()));
                }
                else
                {
                    if (!stateMachine.getStates().containsKey(value))
                    {
                        throw new InvalidExpressionException("Invalid state for state machine '" + stateMachineName + ": " + ExpressionUtil.renderExpressionOf(node));
                    }

                    ctx.output(generator.quote(value));
                }
            }
        }
        else
        {
            throw new InconsistentModelException("Invalid state machine expression: " + ExpressionUtil.renderExpressionOf(second));
        }
    }


    private void renderIsValid(
        ExpressionTransformationContext ctx, ASTFunction node
    )
    {
        Node first = node.jjtGetChild(0);
        Node second = node.jjtGetChild(1);

        final PropertyModel firstModel = getModel(first);
        final PropertyModel secondModel = getModel(second);

        final String firstStateMachine = firstModel.getTypeParam();
        final String secondStateMachine = secondModel.getTypeParam();
        if (!firstStateMachine.equals(secondStateMachine))
        {
            throw new InvalidExpressionException("isValidTransition: State machine mismatch: " + firstStateMachine + " and " + secondStateMachine);
        }

        ctx.output("_v.isValidTransition(" + SingleQuoteJSONGenerator.INSTANCE.forValue(firstStateMachine) + ",");

        ctx.applyRecursive(first);
        ctx.output(",");
        ctx.applyRecursive(second);
        ctx.output(")");


    }

    private PropertyModel getModel(Node node)
    {
        final PropertyModel propertyType = node.annotation().getPropertyType();
        if (propertyType == null || !propertyType.getType().equals(PropertyType.STATE))
        {
            throw new InvalidExpressionException("isValidTransition(State,State): parameter is not a state machine state: " + ExpressionUtil.renderExpressionOf(node));
        }

        return propertyType;
    }
}
