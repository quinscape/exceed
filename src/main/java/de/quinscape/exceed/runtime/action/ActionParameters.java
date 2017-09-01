package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;

public interface ActionParameters
{
    List<Object> get(RuntimeContext runtimeContext, ActionRegistration registration, List<DomainProperty> propertyModels);
}
