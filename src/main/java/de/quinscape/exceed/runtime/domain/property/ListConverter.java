package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.component.DataGraphType;
import jdk.nashorn.api.scripting.JSObject;
import netscape.javascript.JSUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ListConverter
    implements PropertyConverter<List, List,Object>
{
    private final PropertyConverter elementConverter;

    private final String elementType;


    public ListConverter(PropertyConverter elementConverter, String elementType)
    {
        this.elementConverter = elementConverter;
        this.elementType = elementType;
    }


    @Override
    public List convertToJava(RuntimeContext runtimeContext, List value)
    {
        List converted = new ArrayList(value.size());
        for (Object o : value)
        {
            converted.add(
                elementConverter.convertToJava(runtimeContext, o)
            );
        }
        return converted;
    }


    @Override
    public List convertToJSON(RuntimeContext runtimeContext, List value)
    {
        List converted = new ArrayList(value.size());
        for (Object o : value)
        {
            converted.add(
                elementConverter.convertToJSON(runtimeContext, o)
            );
        }
        return converted;
    }


    @Override
    public Class<List> getJavaType()
    {
        return List.class;
    }


    @Override
    public Class<List> getJSONType()
    {
        return List.class;
    }


    @Override
    public Object convertToJs(RuntimeContext runtimeContext, List list)
    {
        final ArrayList converted = new ArrayList(list.size());
        for (int i = 0; i < list.size(); i++)
        {
            Object value = list.get(i);

            converted.add(
                elementConverter.convertToJs(runtimeContext, value)
            );
        }
        return converted;
    }


    @Override
    public List convertFromJs(RuntimeContext runtimeContext, Object value)
    {
        if (value instanceof JSObject)
        {
            final JSObject jsObject = (JSObject) value;
            final int count = (int) jsObject.getMember("length");
            List<Object> converted = new ArrayList<>(count);
            for (int i=0; i < count ; i++)
            {
                converted.add(
                    elementConverter.convertFromJs(runtimeContext, jsObject.getSlot(i))
                );
            }

            return converted;
        }
        else if (value instanceof Collection)
        {
            Collection<Object> collection = (Collection<Object>)value;
            List<Object> converted = new ArrayList<>(collection.size());

            for (Object elem : collection)
            {
                converted.add(
                    elementConverter.convertFromJs(runtimeContext, elem)
                );
            }

            return converted;
        }
        else if (value instanceof DataGraph)
        {
            return ((DataGraph) value).extractList(runtimeContext, elementType);
        }
        else
        {
            throw new IllegalArgumentException("Cannot convert "+ value);
        }

    }
}
