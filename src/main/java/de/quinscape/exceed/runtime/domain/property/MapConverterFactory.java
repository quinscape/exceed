package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MapConverterFactory
    implements PropertyConverterFactory
{
    private final static Logger log = LoggerFactory.getLogger(MapConverterFactory.class);


    @Override
    public PropertyConverter<?, ?, ?> create(
        ApplicationModel applicationModel,
        PropertyTypeModel propertyTypeModel,
        String valueType,
        Map<String, Object> config
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
            final PropertyTypeModel propertyType = applicationModel.getPropertyType(valueType);

            elementConverter = propertyType.createConverter(applicationModel, null, config);
        }
        else
        {
            throw new InconsistentModelException("Map value type '" + valueType + "' is neither a registered domain type nor a registered property type");
        }
        return new MapConverter(elementConverter, valueType);
    }
}
