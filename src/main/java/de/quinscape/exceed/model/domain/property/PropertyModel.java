package de.quinscape.exceed.model.domain.property;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.state.StateMachine;

import java.util.Map;

public interface PropertyModel
{
    String getType();

    String getDefaultValue();

    ASTExpression getDefaultValueExpression();

    String getTypeParam();

    int getMaxLength();

    String getName();

    String getDescription();

    PropertyType getPropertyType();

    void setPropertyType(PropertyType propertyType);

    <T> T getConfig(String name, T defaultValue);

    Map<String, Object> getConfig();
    
    static void initDefaults(ScopedPropertyModel property)
    {
        final String propertyName = property.getName();
        if (DomainType.ID_PROPERTY.equals(propertyName))
        {
            property.setRequired(true);
        }

        if (PropertyType.UUID.equals(property.getType()) && property.getMaxLength() <= 0)
        {
            property.setMaxLength(36);
        }

        if (PropertyType.STATE.equals(property.getType()) && property.getDefaultValue() == null)
        {
            property.setDefaultValue(property.getTypeParam() + "." + StateMachine.START);
        }
    }
}
