package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;

public class VarsPropertyTypeResolver
    extends ComponentPropertyTypeResolver
{

    @Override
    protected PropertyModel resolve(TypeAnalyzerContext context, ResolvableNode node, ComponentDescriptor contextModel)
    {
        return null;
    }


    @Override
    protected String getName()
    {
        return "vars";
    }


    @Override
    protected PropertyModel resolve(TypeAnalyzerContext context, ResolvableNode node, ComponentModel componentModel)
    {
//        final ComponentRegistration registration = componentModel.getComponentRegistration();
//        if (registration != null)
//        {
//            final Node parent = node.jjtGetParent();
//            if (!(parent instanceof ASTPropertyChain && parent.jjtGetNumChildren() == 2))
//            {
//                throw new InconsistentModelException("Invalid vars expression:" + ExpressionUtil
//                    .renderExpressionOf(node));
//            }
//
//            final String name = ExpressionUtil.getNameOrValue(parent.jjtGetChild(1));
//
//            final Map<String, ExpressionValue> vars = registration.getDescriptor().getVars();
//
//            final Object value = vars.get(name);
//
//            // we assume vars to have a fixed type
//            return ExpressionUtil.getPropertyModelFor(value.getClass());
//        }
        return null;
    }

}
