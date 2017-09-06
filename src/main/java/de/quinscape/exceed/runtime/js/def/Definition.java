package de.quinscape.exceed.runtime.js.def;

import de.quinscape.exceed.expression.ResolvableNode;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.runtime.js.TypeAnalyzerContext;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;

public interface Definition
    extends Comparable<Definition>
{
    String getName();

    String getDescription();

    String getChapter();
    
    DefinitionType getDefinitionType();

    PropertyModel getType(TypeAnalyzerContext context, ResolvableNode astFunction, ExpressionModelContext contextModel);

    boolean isFunction();

    static DefinitionsBuilder builder()
    {
        return new DefinitionsBuilder();
    }

    DefinitionRenderer getDefinitionRenderer();
}
