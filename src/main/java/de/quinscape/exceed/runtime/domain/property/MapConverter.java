package de.quinscape.exceed.runtime.domain.property;

import com.google.common.collect.Maps;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import jdk.nashorn.api.scripting.JSObject;

import java.util.HashMap;
import java.util.Map;

class MapConverter
    implements PropertyConverter<Map, Map, Object>
{
    private final PropertyConverter elementConverter;

    public MapConverter(PropertyConverter elementConverter, String typeParam)
    {
        this.elementConverter = elementConverter;
    }

    @Override
    public Map convertToJava(RuntimeContext runtimeContext, Map value)
    {
        final Map<String,Object> converted = new HashMap<>(value.size());
        final Map<String,Object> in = value;

        for (Map.Entry<String, Object> entry : in.entrySet())
        {
            final String key = entry.getKey();
            converted.put(key,
                elementConverter.convertToJava(runtimeContext, entry.getValue())
            );
        }

        return converted;
    }


    @Override
    public Map convertToJSON(RuntimeContext runtimeContext, Map value)
    {
        final Map<String,Object> converted = new HashMap<>(value.size());
        final Map<String,Object> in = value;

        for (Map.Entry<String, Object> entry : in.entrySet())
        {
            final String key = entry.getKey();
            converted.put(key,
                elementConverter.convertToJSON(runtimeContext, entry.getValue())
            );
        }

        return converted;
    }


    @Override
    public Object convertToJs(RuntimeContext runtimeContext, Map value)
    {
        final JsEnvironment env = runtimeContext.getJsEnvironment();

        Map<String, Object> converted = Maps.newHashMapWithExpectedSize(value.size());

        for (Map.Entry<String, Object> e : converted.entrySet())
        {
            converted.put(e.getKey(), elementConverter.convertToJs(runtimeContext, e.getValue()));
        }
        return converted;
    }


    @Override
    public Map convertFromJs(RuntimeContext runtimeContext, Object value)
    {
        if (value instanceof JSObject)
        {
            Map<String, Object> converted = new HashMap<>();
            final JSObject jsObject = (JSObject) value;
            for (String prop : jsObject.keySet())
            {
                converted.put(
                    prop,
                    elementConverter.convertFromJs(runtimeContext, jsObject.getMember(prop))
                );
            }
            return converted;
        }
        else if (value instanceof Map)
        {
            Map<String, Object> converted = new HashMap<>();
            final Map<String,Object> map = (Map<String,Object>)value;
            for (Map.Entry<String, Object> e : map.entrySet())
            {
                converted.put(
                    e.getKey(),
                    elementConverter.convertFromJs(runtimeContext, e.getValue())
                );
            }
            return converted;
        }
        else
        {
            throw new IllegalArgumentException("Cannot convert" + value);
        }
    }


    @Override
    public Class<Map> getJavaType()
    {
        return Map.class;
    }


    @Override
    public Class<Map> getJSONType()
    {
        return Map.class;
    }

}
