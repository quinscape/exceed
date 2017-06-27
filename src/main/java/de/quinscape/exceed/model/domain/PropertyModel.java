package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;

import java.util.Map;

public interface PropertyModel
{
    String getType();

    String getDefaultValue();

    ASTExpression getDefaultValueExpression();

    Object getTypeParam();

    int getMaxLength();

    String getName();

    String getDescription();
}
