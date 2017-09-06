package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.EnumType;
import de.quinscape.exceed.model.state.StateMachine;

import java.util.Map;

/**
 * Implemented by classes providing access to named domain types.
 */
public interface DomainTypesRegistry
{
    Map<String,DomainType> getDomainTypes();

    Map<String,EnumType> getEnums();

    Map<String,PropertyTypeModel> getPropertyTypes();

    Map<String,StateMachine> getStateMachines();
}
