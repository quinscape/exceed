package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.TopLevelModel;

/**
 * THe logical property types within the application.
 */
public class PropertyType
    extends TopLevelModel
{
    private String converter;
    private Integer defaultLength;

    /**
     * Converter spring bean name for the property type.
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
}
