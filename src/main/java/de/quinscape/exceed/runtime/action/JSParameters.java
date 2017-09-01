package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import jdk.nashorn.api.scripting.JSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ActionParameters implementation for a JSObject array argument;
 */
public class JSParameters
    implements ActionParameters
{
    private final JSObject args;


    public JSParameters(JSObject args)
    {
        if (args == null)
        {
            throw new IllegalArgumentException("args can't be null");
        }

        if (!args.isArray())
        {
            throw new IllegalArgumentException("args is not an array");
        }


        this.args = args;


    }


    @Override
    public List<Object> get(RuntimeContext runtimeContext, ActionRegistration registration, List<DomainProperty>
        propertyModels)
    {
        final int numberOfParameters = propertyModels.size();
        final boolean isVarArgs = registration.isVarArgs();

        final int numArgs = (int) args.getMember("length");

        if (numArgs > numberOfParameters && !isVarArgs)
        {
            throw new InvalidActionParameterException(
                "Action '" + registration.getActionName() + "' takes at most "+ numberOfParameters + " parameters"
            );
        }

        final DomainProperty lastParam = isVarArgs ? propertyModels.get(propertyModels.size() - 1) : null;

        final JsEnvironment env = runtimeContext.getJsEnvironment();
        List<Object> converted = new ArrayList<>();
        for (int i = 0; i < numArgs; i++)
        {
            final Object parameter = args.getSlot(i);

            final DomainProperty propertyModel = i < numberOfParameters ? propertyModels.get(i) : lastParam;

            final PropertyType propertyType = PropertyType.get(
                runtimeContext,
                propertyModel
            );

            converted.add(
                propertyType.convertFromJs(
                    runtimeContext,
                    parameter
                )
            );
        }

        return converted;
    }
}
