package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates JSON-level property values as input for an action invocation
 */
public class JSONParameters
    implements ActionParameters
{
    private final List<Object> args;

    public JSONParameters(List<Object> args)
    {
        this.args = args;
    }

    @Override
    public List<Object> get(RuntimeContext runtimeContext, ActionRegistration registration, List<DomainProperty> propertyModels)
    {
        final int numberOfDeclared = propertyModels.size();
        final int numberOfProvided = args.size();

        if (numberOfProvided > numberOfDeclared && !registration.isVarArgs())
        {
            throw new InvalidActionParameterException("Action '" + registration.getActionName() + "' takes at most " + numberOfDeclared + " parameters");
        }
        final List<Object> converted = new ArrayList<>(numberOfProvided);

        if (numberOfDeclared > 0)
        {
            final int lastIndex = numberOfDeclared - 1;
            final DomainProperty lastParam = propertyModels.get(lastIndex);
            for (int i = 0; i < numberOfProvided; i++)
            {
                DomainProperty propertyModel = i < lastIndex ? propertyModels.get(i) : lastParam;
                PropertyType propertyType = PropertyType.get(runtimeContext, propertyModel);

                Object value = null;
                if (i < args.size())
                {
                    value = args.get(i);
                }
                if (propertyModel.isRequired() && value == null)
                {
                    throw new InvalidActionParameterException("Missing parameter value for parameter #" + i);
                }
                try
                {
                    converted.add(propertyType.convertToJava(runtimeContext, value));
                }
                catch (Exception e)
                {
                    throw new InvalidActionParameterException("Error converting parameter: " + value, e);
                }
            }
        }
        return converted;
    }
}
