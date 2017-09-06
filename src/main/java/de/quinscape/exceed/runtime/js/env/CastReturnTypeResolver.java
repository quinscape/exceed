package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public class CastReturnTypeResolver
    implements PropertyTypeResolver
{
    @Override
    public PropertyModel resolve(
        TypeAnalyzerContext context, ResolvableNode node, ExpressionModelContext contextModel
    )
    {
        if (node.jjtGetNumChildren() != 2)
        {
            throw new InvalidExpressionException("cast(type, value) takes exactly two arguments");
        }

        final Node typeNode = node.jjtGetChild(0);
        if (!(typeNode instanceof ASTString))
        {
            throw new InvalidExpressionException("cast(type, value): type must be a string literal");
        }

        // same type resolution logic here as with collection types. 
        return ExpressionUtil.getCollectionType(context.getApplicationModel(), ((ASTString) typeNode).getValue());
    }
}
