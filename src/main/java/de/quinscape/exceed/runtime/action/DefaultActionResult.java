package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;

public class DefaultActionResult
    implements ActionResult
{
    private final Object value;

    private final DomainProperty propertyModel;

    private final RuntimeContext runtimeContext;

    private final boolean isResolved;


    public DefaultActionResult(
        RuntimeContext runtimeContext,
        Object value,
        DomainProperty propertyModel,
        boolean isResolved
    )
    {
        this.runtimeContext = runtimeContext;
        this.value = value;
        this.propertyModel = propertyModel;
        this.isResolved = isResolved;
    }


    @Override
    public boolean isResolved()
    {
        return isResolved;
    }


    @Override
    public Object toJS()
    {
        if (propertyModel == MethodAccessRegistration.VOID_TYPE)
        {
            return null;
        }

        final PropertyType propertyType = PropertyType.get(
            runtimeContext,
            propertyModel
        );

        return propertyType.convertToJs(runtimeContext, value);
    }


    @Override
    public Object toJSON()
    {
        if (propertyModel == MethodAccessRegistration.VOID_TYPE)
        {
            return null;
        }

        return PropertyType.get(runtimeContext, propertyModel).convertToJSON(runtimeContext, value);
    }


    @Override
    public Object get()
    {
        return value;
    }
}
