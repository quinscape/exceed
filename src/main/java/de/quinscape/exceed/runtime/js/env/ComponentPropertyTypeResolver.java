package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;

/**
 * Resolves property types based on a component model context.
 *
 */
public abstract class ComponentPropertyTypeResolver
    implements PropertyTypeResolver
{
    @Override
    public PropertyModel resolve(TypeAnalyzerContext context, ResolvableNode node, ExpressionModelContext ctx)
    {
        Object contextModel = ctx.getContextModel();

        if (contextModel instanceof ComponentModel)
        {
            return resolve(context, node, (ComponentModel)contextModel);
        }
        else if (contextModel instanceof ComponentDescriptor)
        {
            return resolve(context, node, (ComponentDescriptor)contextModel);
        }
        else
        {
            throw new InvalidExpressionException(getName() + " only valid in component expression");
        }
    }


    protected abstract PropertyModel resolve(TypeAnalyzerContext context, ResolvableNode node, ComponentDescriptor contextModel);


    protected abstract String getName();


    /**
     * Abstract method called to resolve the type
     *
     * @param context           type analyzer context
     * @param node              resolvable node
     * @param componentModel    component model context
     *
     * @return type or <code>null</code> for undetermined
     */
    protected abstract PropertyModel resolve(
        TypeAnalyzerContext context,
        ResolvableNode node,
        ComponentModel componentModel
    );
}
