package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.util.Util;

import java.util.Map;
import java.util.Objects;

/**
 * Key class for the property type cache. We want to find all property types with the same
 * type, typeParam and config (regardless of whether the latter two are <code>null</code>)
 */
final class PropertyTypeKey
{
    public final String type;
    public final Object typeParam;
    public final Map<String, Object> config;

    PropertyTypeKey(String type, Object typeParam, Map<String, Object> config)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }
        this.type = type;
        this.typeParam = typeParam;
        this.config = config;
    }


    @Override
    public int hashCode()
    {
        return Util.hashcodeOver(type, typeParam, config);
    }


    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj instanceof PropertyTypeKey)
        {
            PropertyTypeKey that = (PropertyTypeKey)obj;

            return
                type.equals(that.type) &&
                Objects.equals(typeParam, that.typeParam) &&
                Objects.equals(config, that.config);
        }
        return false;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "type = '" + type + '\''
            + ", typeParam = " + typeParam
            + ", config = " + config
            ;
    }
}
