package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.runtime.ExceedRuntimeException;

import java.util.Map;

public class EnumConverterFactory
    implements PropertyConverterFactory
{

    @Override
    public PropertyConverter<?, ?, ?> create(
        ApplicationModel applicationModel,
        PropertyTypeModel propertyTypeModel,
        String typeParam,
        Map<String, Object> config
    )
        
    {
        // make sure enum name is valid
        applicationModel.getEnum(typeParam);

        final String javaEnumConfig = getConfigValue(config, EnumConverter.JAVA_ENUM_CONFIG, String.class);
        if (javaEnumConfig != null)
        {
            try
            {
                return new JavaEnumConverter(
                    (Class<? extends Enum>) Class.forName(javaEnumConfig)
                );
            }
            catch (ClassNotFoundException e)
            {
                throw new ExceedRuntimeException("Error creating Java enum class '" + javaEnumConfig + "'", e);
            }
        }

        return new EnumConverter();
    }
}
