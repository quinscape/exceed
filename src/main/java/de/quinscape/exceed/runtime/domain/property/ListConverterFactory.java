package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.model.InconsistentModelException;

import java.util.Map;

public class ListConverterFactory
    implements PropertyConverterFactory
{

    @Override
    public PropertyConverter<?, ?, ?> create(
        ApplicationModel applicationModel,
        PropertyTypeModel propertyTypeModel,
        String valueType,
        Map<String, Object>config
    )
    {
        if (valueType == null)
        {
            valueType = PropertyType.OBJECT;
        }

        PropertyConverter elementConverter;
        if (applicationModel.getDomainTypes().containsKey(valueType))
        {
            elementConverter = new DomainObjectConverter(valueType, DomainObjectConverter.getImplementationClass(config));
        }
        else if (applicationModel.getPropertyTypes().containsKey(valueType))
        {
            elementConverter = applicationModel.getPropertyType(valueType).createConverter(applicationModel, null, config);
        }
        else
        {
            throw new InconsistentModelException("List value type '" + valueType + "' is neither a registered domain type nor a registered property type");
        }
        return new ListConverter(elementConverter, valueType);
    }


}
