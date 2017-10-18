package de.quinscape.exceed.model.meta;

import de.quinscape.exceed.runtime.util.Util;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulates all properties relevant to the equality of property types.
 *
 * <dl>
 *     <dt>type</dt>
 *     <dd>Property type name</dd>
 *     <dt>typeParam</dt>
 *     <dd>"typeParam" property of the property model</dd>
 *     <dt>config</dt>
 *     <dd>config map for the property model</dd>
 * </dl>
 */
public final class PropertyTypeKey
{
    private final String type;
    private final String typeParam;
    private final Map<String, Object> config;

    public PropertyTypeKey(String type, String typeParam, Map<String, Object> config)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }
        this.type = type;
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
        return type;
    }


    public String getTypeParam()
    {
        return typeParam;
    }


    public Map<String, Object> getConfig()
    {
        return config;
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
                config.equals(that.config);
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
