package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.config.DecimalConfig;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;

import java.util.Map;

public class DecimalConverterFactory
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
        final DecimalConfig decimalConfig = applicationModel.getConfigModel().getDecimalConfig();

        return new DecimalConverter(
            DecimalConverter.getDecimalPlaces(decimalConfig.getDefaultDecimalPlaces(), config),
            decimalConfig.getRoundingMode().getJavaRoundingMode()
        );
    }
}
