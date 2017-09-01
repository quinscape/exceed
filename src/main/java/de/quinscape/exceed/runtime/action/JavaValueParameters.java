package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.List;

/**
 * Action service parameter input with Java side properties
 */
public class JavaValueParameters
    implements ActionParameters
{
    private final List<Object> parameters;


    public JavaValueParameters(List<Object> parameters)
    {
        this.parameters = parameters;
    }


    /**
     * Returns the arguments with conversion since they're already java-side properties
     *  @param runtimeContext        not used
     * @param propertyModels        not used  @return  parameters
     */
    @Override
    public List<Object> get(RuntimeContext runtimeContext, ActionRegistration registration, List<DomainProperty> propertyModels)
    {
        final int numberOfDeclared = propertyModels.size();
        final int numberOfProvided = parameters.size();

        final boolean isVarArgs = registration.isVarArgs();
        if (numberOfProvided > numberOfDeclared && !isVarArgs)
        {
            throw new InvalidActionParameterException("Action '" + registration.getActionName() + "' takes at most " + numberOfDeclared + " parameters");
        }

        final int lastIndex = numberOfDeclared - 1;
        final DomainProperty lastParam = propertyModels.get(lastIndex);

        for (int i = 0; i < parameters.size(); i++)
        {
            Object parameter =  parameters.get(i);

            final Class javaType = (i < lastIndex ? propertyModels.get(i) : lastParam).getPropertyType().getJavaType();

            if (!javaType.isInstance(parameter))
            {
                throw new InvalidActionParameterException("Parameter" + parameter + " is not assignable to " + javaType);
            }

        }

        return parameters;
    }
}
