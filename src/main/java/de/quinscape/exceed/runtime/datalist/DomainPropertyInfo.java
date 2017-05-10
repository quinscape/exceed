package de.quinscape.exceed.runtime.datalist;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;

public class DomainPropertyInfo
{
    private final DomainProperty property;

    private final PropertyConverter propertyConverter;


    public DomainPropertyInfo(DomainProperty property, PropertyConverter propertyConverter)
    {
        if (property == null)
        {
            throw new IllegalArgumentException("property can't be null");
        }

        if (propertyConverter == null)
        {
            throw new IllegalArgumentException("propertyConverter can't be null");
        }

        this.property = property;
        this.propertyConverter = propertyConverter;
    }


    public DomainProperty getProperty()
    {
        return property;
    }


    public PropertyConverter getPropertyConverter()
    {
        return propertyConverter;
    }
}
