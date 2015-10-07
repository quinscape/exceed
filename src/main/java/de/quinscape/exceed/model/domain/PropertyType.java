package de.quinscape.exceed.model.domain;

import de.quinscape.exceed.model.TopLevelModel;

public class PropertyType
    extends TopLevelModel
{
    private String converter;
    private Integer defaultLength;

    public String getConverter()
    {
        return converter;
    }

    public void setConverter(String converter)
    {
        this.converter = converter;
    }

    public Integer getDefaultLength()
    {
        return defaultLength;
    }

    public void setDefaultLength(Integer defaultLength)
    {
        this.defaultLength = defaultLength;
    }
}
