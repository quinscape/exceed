package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;

import java.util.Map;

public class CurrencyConverterFactory
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
        final String currency;
        if (typeParam != null)
        {
            currency = typeParam;
        }
        else
        {
            currency = applicationModel.getConfigModel().getDefaultCurrency();
        }
        return new CurrencyConverter(currency);
    }
}
