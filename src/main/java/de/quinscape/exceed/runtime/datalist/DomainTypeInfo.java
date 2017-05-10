package de.quinscape.exceed.runtime.datalist;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;

import java.util.HashMap;
import java.util.Map;

public class DomainTypeInfo
{
    private Map<String, DomainPropertyInfo> infos;

    public DomainTypeInfo(DomainType domainType, Map<String, PropertyConverter> propertyConverters)
    {
        infos = new HashMap<>();
        for (DomainProperty property : domainType.getProperties())
        {
            final String propertyName = property.getName();
            final String converterName = propertyName + "Converter";
            infos.put(propertyName, new DomainPropertyInfo(property, propertyConverters.get(converterName)));
        };

    }
}
