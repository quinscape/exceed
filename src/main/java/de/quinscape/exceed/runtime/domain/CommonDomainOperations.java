package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.js.JsEnvironment;

public final class CommonDomainOperations
{
    private CommonDomainOperations()
    {
        
    }

    public static DomainObject create(
        RuntimeContext runtimeContext,
        DomainService domainService,
        String type,
        String id,
        Class<? extends DomainObject> implClass
    )
    {
        final DomainType domainType = domainService.getDomainType(type);

        if (domainType == null)
        {
            throw new IllegalArgumentException("Unknown domain type '" + type + "'");
        }
        DomainObject genericDomainObject;
        if (implClass == null)
        {
            genericDomainObject = new GenericDomainObject();
        }
        else
        {
            try
            {
                genericDomainObject = implClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                throw new ExceedRuntimeException("Error creating domainObject with type " + type + " and id " + id, e);
            }
        }

        if (!GeneratedDomainObject.class.isAssignableFrom(implClass))
        {
            genericDomainObject.setDomainType(type);
        }
        genericDomainObject.setId(id);
        genericDomainObject.setDomainService(domainService);


        final JsEnvironment env = runtimeContext.getJsEnvironment();

        for (DomainProperty property : domainType.getProperties())
        {
            final ASTExpression defaultValueExpression = property.getDefaultValueExpression();
            if (defaultValueExpression != null)
            {
                final Object defaultValue = env.getValue(runtimeContext, defaultValueExpression);
                genericDomainObject.setProperty(property.getName(), defaultValue);
            }
        }
        return genericDomainObject;
    }
}
