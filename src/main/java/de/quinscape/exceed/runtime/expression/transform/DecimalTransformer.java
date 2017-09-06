package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTDecimal;
import de.quinscape.exceed.expression.ArithmeticOperatorNode;
import de.quinscape.exceed.expression.ComparatorNode;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.Operator;
import de.quinscape.exceed.expression.OperatorNode;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public class DecimalTransformer
    implements ExpressionTransformer
{
    private final Set<String> decimalTypes;

    public DecimalTransformer(ApplicationModel applicationModel)
    {
        this.decimalTypes = applicationModel.getMetaData().getPropertyTypes().values().stream()
            .filter(
                type -> type.getJavaType().equals(BigDecimal.class)
            ).map(PropertyType::getType)
            .collect(Collectors.toSet());
    }


    @Override
    public boolean appliesTo(ExpressionTransformationContext ctx, Node node)
    {
        if (node instanceof ComparatorNode)
        {
            return
                isAnnotatedAsDecimal(node.jjtGetChild(0)) ||
                isAnnotatedAsDecimal(node.jjtGetChild(1));
        }
        else
        {
            return isAnnotatedAsDecimal(node);
        }
    }

    private boolean isAnnotatedAsDecimal(Node node)
    {
        final PropertyModel propertyType = node.annotation().getPropertyType();
        return propertyType != null && decimalTypes.contains(propertyType.getType());
    }


    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {
        //final PropertyModel propertyType = node.annotation().getPropertyType();

        if (node instanceof ComparatorNode)
        {
            final ComparatorNode comparator = (ComparatorNode) node;
            if (comparator.getOperator() == Operator.NOT_EQUALS)
            {
                // we have no "notEquals" in bignumber.js, we need to invert the ".equals"
                ctx.output("!");
            }
            ctx.output("(");
            renderAsDecimal(ctx, node.jjtGetChild(0));
            ctx.output(getDecimalOperand(ctx, comparator.getOperator()));
            renderAsDecimal(ctx, node.jjtGetChild(1));
            ctx.output(")");
        }
        else if (node instanceof ArithmeticOperatorNode)
        {
            final Operator op = ((OperatorNode) node).getOperator();

            final int numChildren = node.jjtGetNumChildren();
            ctx.output("(");
            for (int i=0; i < numChildren; i++)
            {
                Node kid = node.jjtGetChild(i);

                if (i > 0)
                {
                    ctx.output(getDecimalOperand(ctx, op));
                }
                renderAsDecimal(ctx, kid);
           }
            ctx.output(")");
        }
        else if (node instanceof ASTDecimal)
        {
            ctx.output("_decimal('" + ((ASTDecimal) node).getValue().toString() + "')");
        }
        else
        {
            ctx.renderDefault();
        }
    }


    private void renderAsDecimal(ExpressionTransformationContext ctx, Node kid)
    {
        final PropertyModel propertyType = kid.annotation().getPropertyType();
        if (propertyType != null && decimalTypes.contains(propertyType.getType()))
        {
            ctx.applyRecursive(kid);
        }
        else
        {
            ctx.output("_decimal(");
            ctx.applyRecursive(kid);
            ctx.output(")");
        }

    }


    private String getDecimalOperand(ExpressionTransformationContext ctx, Operator op)
    {
        switch(op)
        {
            case EQUALS:
            case NOT_EQUALS:    // we inverted for this
                return ").equals(";
            case LESS:
                return ").lt(";
            case LESS_OR_EQUALS:
                return ").lte(";
            case GREATER:
                return ").gt(";
            case GREATER_OR_EQUALS:
                return ").gte(";
            case ADD:
                return ").add(";
            case SUBTRACT:
                return ").sub(";
            case MULTIPLY:
                return ").mul(";
            case DIVIDE:
                return ").div(";
            default:
                return op.getAsString();
        }
    }
}
