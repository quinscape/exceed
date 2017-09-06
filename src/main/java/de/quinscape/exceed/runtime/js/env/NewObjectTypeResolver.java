package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

import java.util.Map;

public class NewObjectTypeResolver
    implements PropertyTypeResolver
{
    @Override
    public PropertyModel resolve(TypeAnalyzerContext context, ResolvableNode node, ExpressionModelContext ctx)
    {
        final Map<String, DomainType> domainTypes = context.getApplicationModel().getDomainTypes();

        Object contextModel = ctx.getContextModel();
        if (contextModel instanceof ScopedPropertyModel && ((ScopedPropertyModel) contextModel).getType().equals(PropertyType.DOMAIN_TYPE))
        {
            final String typeParam = ((ScopedPropertyModel) contextModel).getTypeParam();
            if (domainTypes.containsKey(typeParam))
            {
                return DomainProperty.builder()
                    .withType(PropertyType.DOMAIN_TYPE, typeParam)
                    .build();
            }
            else
            {
                throw new InvalidExpressionException("newObject(): '" + typeParam + "' is not a valid domain type");
            }
        }

        if (node.jjtGetNumChildren() == 1)
        {
            final Node firstArg = node.jjtGetChild(0);

            if(firstArg instanceof ASTString)
            {
                final String name = ((ASTString) firstArg).getValue();
                if(domainTypes.containsKey(name))
                {
                    return DomainProperty.builder()
                        .withType(PropertyType.DOMAIN_TYPE, name)
                        .build();
                }
            }
        }
        else
        {
            final Node parent = node.jjtGetParent();
            if (parent instanceof ASTAssignment)
            {
                final Node lValue = parent.jjtGetChild(0);
                if (lValue instanceof ASTIdentifier)
                {
                    final Definition definition = context.getContextDefinitions().getDefinition(
                        ((ASTIdentifier) lValue).getName());

                    if (definition.getDefinitionType() == DefinitionType.CONTEXT)
                    {
                        final PropertyModel type = definition.getType(
                            context, (ASTIdentifier) lValue, context.getContextModel()
                        );

                        if (type.getType().equals(PropertyType.DOMAIN_TYPE))
                        {
                            return type;
                        }
                    }
                }
            }
        }

        throw new InvalidExpressionException("Cannot resolve type of newObject expression: " + ExpressionUtil.renderExpressionOf(node) + ", " + contextModel);
    }
}
