package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.domain.property.BooleanConverter;
import de.quinscape.exceed.runtime.domain.property.DateConverter;
import de.quinscape.exceed.runtime.domain.property.DomainTypeConverter;
import de.quinscape.exceed.runtime.domain.property.EnumConverter;
import de.quinscape.exceed.runtime.domain.property.IntegerConverter;
import de.quinscape.exceed.runtime.domain.property.LongConverter;
import de.quinscape.exceed.runtime.domain.property.ObjectConverter;
import de.quinscape.exceed.runtime.domain.property.PlainTextConverter;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.domain.property.RichTextConverter;
import de.quinscape.exceed.runtime.domain.property.TimestampConverter;
import de.quinscape.exceed.runtime.domain.property.UUIDConverter;

import java.util.HashMap;
import java.util.Map;

public class DefaultPropertyConverters
{
    public Map<String, PropertyConverter> getConverters()
    {
        Map<String, PropertyConverter> map = new HashMap<>();

        add(map, new BooleanConverter());
        add(map, new DateConverter());
        add(map, new EnumConverter());
        add(map, new IntegerConverter());
        add(map, new LongConverter());
        add(map, new ObjectConverter());
        add(map, new PlainTextConverter());
        add(map, new RichTextConverter());
        add(map, new TimestampConverter());
        add(map, new UUIDConverter());
        add(map, new DomainTypeConverter());

        return map;
    }

    private void add(Map<String, PropertyConverter> map, PropertyConverter c)
    {
        map.put(c.getClass().getSimpleName(), c);
    }
}

