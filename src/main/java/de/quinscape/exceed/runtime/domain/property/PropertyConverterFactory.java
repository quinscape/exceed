package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;

import java.util.Map;

public interface PropertyConverterFactory
{
    PropertyConverter<?,?, ?> create(ApplicationModel applicationModel, PropertyTypeModel propertyTypeModel, String typeParam, Map<String, Object> config);

    default <T> T getConfigValue(Map<String,Object> config, String name, Class<T> cls)
    {
        if (config == null)
        {
            return null;
        }

        final Object value = config.get(name);
        if (value == null)
        {
            return null;
        }

        if (!cls.isAssignableFrom(value.getClass()))
        {
            throw new InvalidPropertyConfigurationException("Configuration value '" + name + "' is not a " + cls);
        }
        return (T) value;
    }
}
