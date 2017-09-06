package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.expression.transform.ExpressionTransformationContext;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.def.DefinitionRenderer;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import de.quinscape.exceed.runtime.util.JSONUtil;

public class NewObjectDefinitionRenderer
    implements DefinitionRenderer
{
    @Override
    public void render(ExpressionTransformationContext ctx, Node node)
    {
        final PropertyModel propertyType = node.annotation().getPropertyType();
        if (propertyType != null && propertyType.getType().equals(PropertyType.DOMAIN_TYPE))
        {
            ctx.output("_domainService.create(");
            ctx.output(JSONUtil.DEFAULT_GENERATOR.quote(propertyType.getTypeParam()));
            ctx.output(")");
        }
        else
        {
            if (node.jjtGetNumChildren() < 1)
            {
                throw new InvalidExpressionException("create(type) needs a type parameter: " + ExpressionUtil.renderExpressionOf(node));
            }
            ctx.output("_domainService.create(");
            ctx.applyRecursive(node.jjtGetChild(0));
            ctx.output(")");
        }
    }
}
