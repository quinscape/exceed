package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.EnumType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.state.StateMachine;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.QueryError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates intermediary data results from the component queries and component query errors, also the registered translation keys for the
 * current component tree
 */
public class ViewData
{
    private final Map<String, ComponentData> componentData;

    private final Set<String> translations;

    private Map<String, List<QueryError>> errors;

    public ViewData()
    {
        componentData = new HashMap<>();
        translations = new HashSet<>();
        errors = new HashMap<>();
    }

    public void provide(String componentId, ComponentData componentData)
    {
        this.componentData.put(componentId, componentData);

        for (QueryError error : componentData.getErrors())
        {
            List<QueryError> errorList = errors.get(componentId);

            if (errorList == null)
            {
                errorList = new ArrayList<>();
                errors.put(componentId, errorList);
            }
            errorList.add(error);
        }
    }

    public Map<String, ComponentData> getComponentData()
    {
        return componentData;
    }


    public void registerTranslation(String tag)
    {
        translations.add(tag);
    }


    public Set<String> getTranslations()
    {
        return translations;
    }


    public Map<String, List<QueryError>> getErrors()
    {
        return errors;
    }


    public void registerTranslation(RuntimeContext runtimeContext, PropertyModel propertyModel)
    {
        if (propertyModel instanceof DomainProperty)
        {
            registerTranslation(
                ((DomainProperty) propertyModel).getTranslationTag()
            );
        }
        else if (propertyModel.getType().equals(PropertyType.ENUM))
        {
            final String enumTypeName = propertyModel.getTypeParam();
            if (!getTranslations().contains(enumTypeName))
            {
                final EnumType enumType = runtimeContext.getApplicationModel().getEnum(enumTypeName);
                final List<String> values = enumType.getValues();

                registerTranslation(enumTypeName);
                registerElements(enumType, values);
            }

        }
        else if (propertyModel.getType().equals(PropertyType.STATE))
        {
            final String stateMachineName = propertyModel.getTypeParam();
            if (!getTranslations().contains(stateMachineName))
            {
                final StateMachine stateMachine = runtimeContext.getApplicationModel().getStateMachines().get(stateMachineName);
                final Set<String> values = stateMachine.getStates().keySet();

                registerTranslation(stateMachineName);
                registerElements(stateMachine, values);

                for (String value : values)
                {
                    registerTranslation("Set" + stateMachineName + " " + value);
                }
            }
        }
        else if (propertyModel.getType().equals(PropertyType.DOMAIN_TYPE))
        {
            final String domainTypeName = propertyModel.getTypeParam();

            if (domainTypeName != null && !getTranslations().contains(domainTypeName))
            {
                final DomainType domainType = runtimeContext.getApplicationModel().getDomainType(domainTypeName);
                registerTranslations(runtimeContext, domainType);
            }
        }
    }

    private void registerElements(TopLevelModel topLevelModel, Collection<String> values)
    {
        for (String value : values)
        {
            registerTranslation(topLevelModel.getName() + " " + value);
        }
    }


    public void registerTranslations(RuntimeContext runtimeContext, DomainType type)
    {
        registerTranslation(type.getName());

        for (DomainProperty domainProperty : type.getProperties())
        {
            registerTranslation(runtimeContext, domainProperty);
        }
    }
}
