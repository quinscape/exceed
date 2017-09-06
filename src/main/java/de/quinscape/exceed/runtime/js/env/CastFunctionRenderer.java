package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.expression.transform.ExpressionTransformationContext;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.def.DefinitionRenderer;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.svenson.util.JSONBuilder;

public class CastFunctionRenderer
    implements DefinitionRenderer
{
    @Override
    public void render(
        ExpressionTransformationContext ctx, Node node
    )
    {
        if (node.jjtGetNumChildren() != 2)
        {
            throw new InvalidExpressionException("cast(type,value) takes exactly two parameters");
        }

        Node first = node.jjtGetChild(0);
        Node second = node.jjtGetChild(1);

        if (!(first instanceof ASTString))
        {
            throw new InvalidExpressionException("cast(type,value): type must be string literal");
        }

        final PropertyModel firstModel = ExpressionUtil.getCollectionType(ctx.getApplicationModel(), ( (ASTString) first).getValue());
        final PropertyModel secondModel = second.annotation().getPropertyType();

        String firstVar = pushPropertyType(ctx, firstModel);
        String secondVar = pushPropertyType(ctx, secondModel);

        ctx.output("_v.cast(");
        ctx.output(firstVar);
        ctx.output(",");
        ctx.output(secondVar);
        ctx.output(",");
        ctx.applyRecursive(second);
        ctx.output(")");
    }


    private String pushPropertyType(ExpressionTransformationContext ctx, PropertyModel firstModel)
    {
        final String code;
        if (firstModel == null)
        {
            return "null";
        }
        else
        {
            code = JSONBuilder.buildObject()
                .property("type", firstModel.getType())
                .property("typeParam", firstModel.getTypeParam())
                .output();
            return ctx.pushCodeBlock("pt" + firstModel.getType(), code);
        }
    }
}
