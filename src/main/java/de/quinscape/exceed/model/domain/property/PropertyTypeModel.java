package de.quinscape.exceed.model.domain.property;

import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.domain.property.PropertyConverterFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * THe logical property types within the application.
 */
public class PropertyTypeModel
    extends AbstractTopLevelModel
{

    /**
     * Class name of either a {@link PropertyConverter} or {@link PropertyConverterFactory} implementation
     */
    private String converter;

    /**
     * Default length if applicable.
     */
    private Integer defaultLength;

    private Map<String,Object> defaultConfig = Collections.emptyMap();


    /**
     * Returns the class name of either a {@link PropertyConverter} or {@link PropertyConverterFactory} implementation.
     * @return
     */
    public String getConverter()
    {
        return converter;
    }

    public void setConverter(String converter)
    {
        this.converter = converter;
    }


    /**
     * Default length for this property type.
     * @return
     */
    public Integer getDefaultLength()
    {
        return defaultLength;
    }

    public void setDefaultLength(Integer defaultLength)
    {
        this.defaultLength = defaultLength;
    }


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        return visitor.visit(this,in);
    }

    /**
     * Creates a new property converter for the given property configuration.
     * <p>
     *     The given config map will be merged with the {@link #defaultConfig} specified in the property type model.
     * </p>
     *
     * @param applicationModel      application model
     * @param typeParam             typeParam property value
     * @param config                property configuration map.
     *
     * @return property converter
     */
    public PropertyConverter<?, ?, ?> createConverter(ApplicationModel applicationModel, String typeParam, Map<String, Object> config)
    {
        try
        {
            final Class<?> cls = Class.forName(converter);

            if (PropertyConverter.class.isAssignableFrom(cls))
            {
                return (PropertyConverter<?, ?, ?>) cls.newInstance();
            }
            else if (PropertyConverterFactory.class.isAssignableFrom(cls))
            {
                PropertyConverterFactory factory = (PropertyConverterFactory) cls.newInstance();
                return factory.create(applicationModel, this, typeParam, mergeConfig(config));
            }
            else
            {
                throw new IllegalStateException("Invalid converter class:" + cls);
            }
        }
        catch (Exception e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private Map<String, Object> mergeConfig(Map<String, Object> config)
    {
        if (config == null)
        {
            config = Collections.emptyMap();
        }

        final boolean defaultsEmpty = defaultConfig.size() == 0;
        final boolean configEmpty = config.size() == 0;

        if (configEmpty)
        {
            return defaultConfig;
        }
        else if (defaultsEmpty)
        {
            return config;
        }
        else
        {
            Map<String, Object> merged = new HashMap<>(defaultConfig.size() + config.size());
            merged.putAll(defaultConfig);
            merged.putAll(config);
            return merged;
        }
    }


    /**
     * Default configuration map for this property type model.
     */
    public Map<String, Object> getDefaultConfig()
    {
        return defaultConfig;
    }


    public void setDefaultConfig(Map<String, Object> defaultConfig)
    {
        if (defaultConfig != null)
        {
            this.defaultConfig = defaultConfig;
        }
        else
        {
            this.defaultConfig = Collections.emptyMap();
        }
    }
}
