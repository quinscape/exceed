package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.config.DecimalConfig;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.state.StateMachine;

import java.util.Map;

public class StateConverterFactory
    implements PropertyConverterFactory
{
    @Override
    public PropertyConverter<?, ?, ?> create(
        ApplicationModel applicationModel,
        PropertyTypeModel propertyTypeModel,
        String typeParam,
        Map<String, Object> config
    )
    {
        final StateMachine stateMachine = applicationModel.getStateMachines().get(typeParam);
        if (stateMachine == null)
        {
            throw new IllegalArgumentException(typeParam + " is not a valid state machine name.");
        }

        return new StateConverter(
            stateMachine
        );
    }
}
