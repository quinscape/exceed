package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.GeneratedDomainObject;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;

import java.util.Map;

public class DomainObjectConverterFactory
    implements PropertyConverterFactory
{

    @Override
    public PropertyConverter<?, ?, ?> create(ApplicationModel applicationModel, PropertyTypeModel propertyTypeModel, String type, Map<String, Object>
        config)
    {
        final String name = (String) config.get(DomainObjectConverter.IMPLEMENTATION_CONFIG);
        if (name != null)
        {
            final Class<?> cls;
            try
            {
                cls = Class.forName(name);
            }
            catch (ClassNotFoundException e)
            {
                throw new ExceedRuntimeException("Domain class implementation not found", e);
            }
            if (!DomainObject.class.isAssignableFrom(cls))
            {
                throw new IllegalStateException( name + " does not implement " + DomainObject.class);
            }
            return new DomainObjectConverter(type, (Class<GeneratedDomainObject>) cls);

        }
        return new DomainObjectConverter(type, GenericDomainObject.class);
    }
}
