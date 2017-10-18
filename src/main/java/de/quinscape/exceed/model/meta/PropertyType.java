package de.quinscape.exceed.model.meta;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.property.PropertyConversionException;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a unique configuration of a property type at runtime. It encapsulates the following data:
 *
 * <dl>
 *     <dt>typeParam</dt>
 *     <dd>"typeParam" property field of the property model</dd>
 *     <dt>config</dt>
 *     <dd>Map of config values from the property model</dd>
 *     <dt>propertyTypeModel</dt>
 *     <dd>The property type model referenced by name in the property model</dd>
 *     <dt>converter</dt>
 *     <dd>Converter implementation to use for this property type</dd>
 * </dl>
 *
 * @see ApplicationMetaData#createPropertyType(String, String, Map)
 * @see PropertyTypeKey
 */
public final class PropertyType
    implements PropertyConverter
{
    private final static Logger log = LoggerFactory.getLogger(PropertyType.class);

    public final static String BOOLEAN = "Boolean";

    public final static String INTEGER = "Integer";

    public final static String DECIMAL = "Decimal";

    public final static String PLAIN_TEXT = "PlainText";

    public final static String RICH_TEXT = "RichText";

    public final static String LONG = "Long";

    public final static String TIMESTAMP = "Timestamp";

    public final static String DATE = "Date";

    public final static String OBJECT = "Object";

    public final static String MAP = "Map";

    public final static String LIST = "List";

    public final static String DOMAIN_TYPE = "DomainType";

    public final static String UUID = "UUID";

    public final static String ENUM = "Enum";

    public final static String DATA_LIST_ROOT_PROPERTY_TYPE = "DataListRoot";

    public final static String DATA_GRAPH = "DataGraph";

    public static final String STATE = "State";

    public static final String CURRENCY = "Currency";


    private final String typeParam;

    private final Map<String, Object> config;

    private final PropertyTypeModel propertyTypeModel;

    private final PropertyConverter converter;

    PropertyType(PropertyTypeKey key, PropertyTypeModel propertyTypeModel, PropertyConverter<?, ?, ?> converter)
    {
        this(key.getTypeParam(), key.getConfig(), propertyTypeModel, converter);
    }

    public PropertyType(
        String typeParam,
        Map<String, Object> config,
        PropertyTypeModel propertyTypeModel,
        PropertyConverter<?, ?, ?> converter
    )
    {
        if (propertyTypeModel == null)
        {
            throw new IllegalArgumentException("propertyTypeModel can't be null");
        }

        if (converter == null)
        {
            throw new IllegalArgumentException("converter can't be null");
        }

        this.propertyTypeModel = propertyTypeModel;
        this.converter = converter;

        this.typeParam = typeParam;
        if (config != null)
        {
            this.config = config;
        }
        else
        {
            this.config = Collections.emptyMap();
        }
    }


    public String getType()
    {
        return propertyTypeModel.getName();
    }


    public String getTypeParam()
    {
        return typeParam;
    }


    public Map<String, Object> getConfig()
    {
        if (config == null)
        {
            return Collections.emptyMap();
        }

        return config;
    }


    public PropertyTypeModel getPropertyTypeModel()
    {
        return propertyTypeModel;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + ", type = " + getType()
            + ", typeParam = " + typeParam
            + ", config = " + config
            ;
    }


    @Override
    public Object convertToJava(RuntimeContext runtimeContext, Object value)
    {
        try
        {
            return converter.convertToJava(runtimeContext, value);
        }
        catch(Exception e)
        {
            throw new PropertyConversionException("Error converting " + value + " to java: type = " + this, e);
        }

    }

    @Override
    public Object convertToJSON(RuntimeContext runtimeContext, Object value)
    {
        try
        {
            return converter.convertToJSON(runtimeContext, value);
        }
        catch(Exception e)
        {
            throw new PropertyConversionException("Error converting " + value + " to JSON: type = " + this, e);
        }
    }


    @Override
    public Object convertToJs(RuntimeContext runtimeContext, Object value)
    {
        try
        {
            return converter.convertToJs(runtimeContext, value);
        }
        catch(Exception e)
        {
            throw new PropertyConversionException("Error converting " + value + " to JS: type = " + this, e);
        }
    }


    @Override
    public Object convertFromJs(RuntimeContext runtimeContext, Object value)
    {
        try
        {
            return converter.convertFromJs(runtimeContext, value);
        }
        catch(Exception e)
        {
            throw new PropertyConversionException("Error converting " + value + " from JS: type = " + this, e);
        }
    }


    @JSONProperty(ignore = true)
    public PropertyConverter getConverter()
    {
        return converter;
    }


    @Override
    public Class getJavaType()
    {
        return converter.getJavaType();
    }


    @Override
    public Class getJSONType()
    {
        return converter.getJSONType();
    }


    public static PropertyType get(RuntimeContext runtimeContext, PropertyModel propertyModel)
    {
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();
        return get(applicationModel, propertyModel);
    }

    public static PropertyType get(ApplicationModel applicationModel, PropertyModel propertyModel)
    {
        final PropertyType existing = propertyModel.getPropertyType();
        if (existing != null)
        {
            return existing;
        }

        final ApplicationMetaData metaData = applicationModel.getMetaData();
        final PropertyType newType = metaData.createPropertyType(propertyModel);
        propertyModel.setPropertyType(newType);
        return newType;
    }
}
