package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTPropertyChainDot;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.type.EnumType;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONGenerator;

import java.util.List;
import java.util.Map;

public class EnumValueTransformer
    implements ExpressionTransformer
{
    private final Map<String, EnumType> enumTypes;


    public EnumValueTransformer(ApplicationModel applicationModel)
    {
        this.enumTypes = applicationModel.getEnums();
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
                if ((enumTypes.containsKey(name)))
                {
                    if (node.jjtGetNumChildren() > 2 || !(node.jjtGetChild(1) instanceof ASTPropertyChainDot) || !(node.jjtGetChild(1).jjtGetChild(0) instanceof ASTIdentifier))
                    {
                        throw new InvalidExpressionException(
                            "Invalid enum type expression: " + ExpressionUtil.renderExpressionOf(node));
                    }
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void apply(ExpressionTransformationContext ctx, Node node)
    {

        final String typeName = ((ASTIdentifier) node.jjtGetChild(0)).getName();
        final String value = ((ASTIdentifier) node.jjtGetChild(1).jjtGetChild(0)).getName();

        final EnumType enumType = enumTypes.get(typeName);
        final List<String> values = enumType.getValues();
        for (int i = 0; i < values.size(); i++)
        {
            final String cur = values.get(i);
            if (value.equals(cur))
            {
                ctx.output(i);
                return;
            }
        }

        throw new InvalidExpressionException("Invalid enum value for Enum '" + typeName + "': " + value);
    }
}
