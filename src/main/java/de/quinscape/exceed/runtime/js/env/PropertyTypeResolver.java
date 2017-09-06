package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;

public interface PropertyTypeResolver
{
    PropertyModel resolve(TypeAnalyzerContext context, ResolvableNode node, ExpressionModelContext contextModel);
}
