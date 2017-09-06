package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

public class PropsPropertyTypeResolver
    extends ComponentPropertyTypeResolver
{

    @Override
    protected PropertyModel resolve(TypeAnalyzerContext context, ResolvableNode node, ComponentDescriptor contextModel)
    {
        final Node chain = node.jjtGetParent();
        if (!(chain instanceof ASTPropertyChain))
        {
            return null;
        }
        final String propName = ExpressionUtil.getNameOrValue(chain.jjtGetChild(1).jjtGetChild(0));

        final PropDeclaration propDecl = contextModel.getPropTypes().get(propName);
        if (propDecl != null)
        {
            return mapHack(resolveFromPropDecl(context, null, propName, propDecl));
        }
        return null;
    }


    @Override
    protected String getName()
    {
        return "props";
    }


    @Override
    protected PropertyModel resolve(TypeAnalyzerContext context, ResolvableNode node, ComponentModel componentModel)
    {
        PropertyModel startType;

        final ComponentInstanceRegistration registration = componentModel.getComponentRegistration();

        final Node chain = node.jjtGetParent();
        if (!(chain instanceof ASTPropertyChain))
        {
            return null;
        }
        
        final String propName = ExpressionUtil.getNameOrValue(chain.jjtGetChild(1).jjtGetChild(0));
        if (registration == null)
        {
            return resolveByPropValue(context, propName, componentModel);
        }

        final ComponentDescriptor descriptor = registration.getDescriptor();
        final PropDeclaration propDecl = descriptor.getPropTypes().get(propName);

        if (propDecl == null)
        {
            startType = resolveByPropValue(context, propName, componentModel);
        }
        else
        {
            startType = resolveFromPropDecl(context, componentModel, propName, propDecl);
        }

        if (startType == null)
        {
            return null;
        }


        return mapHack(startType);

    }


    private PropertyModel mapHack(PropertyModel startType)
    {
        String elemType;
        if (startType.getType().equals(PropertyType.DOMAIN_TYPE))
        {
            elemType = startType.getTypeParam();
        }
        else
        {
            elemType = startType.getType();
        }


        // little hack to paint over the fact that we don't have an actual type definition for "props" itself
        // for every props.bla expression we pretend that props to have a map type with the element type bla
        // happens to have
        return DomainProperty.builder()
            .withType(PropertyType.MAP, elemType)
            .build();
    }


    private PropertyModel resolveFromPropDecl(TypeAnalyzerContext context, ComponentModel componentModel, String propName, PropDeclaration propDecl)
    {
        PropertyModel startType;
        switch (propDecl.getType())
        {
            case PLAINTEXT:
            case CLASSES:
            case TRANSITION:
            case DOMAIN_TYPE_REFERENCE:
                startType = ExpressionUtil.PLAINTEXT_TYPE;
                break;

            case INTEGER:
                startType = ExpressionUtil.INTEGER_TYPE;
                break;

            case FLOAT:
                startType = ExpressionUtil.DECIMAL_TYPE;
                break;

            case BOOLEAN:
                startType = ExpressionUtil.DECIMAL_TYPE;
                break;

            case MAP:
                startType = DomainProperty.builder().withType(PropertyType.MAP).build();
                break;

            case QUERY_EXPRESSION:
            case FILTER_EXPRESSION:
            case VALUE_EXPRESSION:
                if (componentModel == null)
                {
                    return null;
                }
                startType = resolveByPropValue(context, propName, componentModel);
                break;

            case ACTION_EXPRESSION:
            case CURSOR_EXPRESSION:
                startType = null;
                break;

            default:
                throw new IllegalStateException("Unhandled type: " + propDecl.getType());
        }

        return startType;
    }


    protected PropertyModel resolveByPropValue(TypeAnalyzerContext context, String propName, ComponentModel componentModel)
    {
        final ExpressionValue value = componentModel.getAttribute(propName);

        if (value == null)
        {
            return null;
        }

        switch(value.getType())
        {
            case EXPRESSION_ERROR:
                return null;

            case STRING:
                return ExpressionUtil.PLAINTEXT_TYPE;

            case EXPRESSION:
                return context.getTypeAnalyzer().analyze(context, value.getAstExpression());

            default:
                throw new IllegalStateException("Unhandled type: " + value.getType());
        }
    }
}
