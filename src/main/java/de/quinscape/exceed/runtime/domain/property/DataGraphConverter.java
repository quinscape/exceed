package de.quinscape.exceed.runtime.domain.property;

import com.google.common.collect.Maps;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import jdk.nashorn.api.scripting.JSObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataGraphConverter
    implements PropertyConverter<DataGraph, Map, Object>
{
    @Override
    public Object convertToJs(RuntimeContext runtimeContext, DataGraph value)
    {
        return value;
    }

    @Override
    public DataGraph convertFromJs(RuntimeContext runtimeContext, Object value)
    {
        if (value instanceof DataGraph)
        {
            return (DataGraph) value;
        }
        else if (value instanceof JSObject)
        {
            final JSObject jsObject = (JSObject) value;
            final JSObject columnsIn = (JSObject) jsObject.getMember("columns");

            Map<String, DomainProperty> columns = new HashMap<>();
            for (String columnName : columnsIn.keySet())
            {
                final DomainProperty domainProperty = DomainProperty.builder()
                    .fromJSObject((JSObject) columnsIn.getMember(columnName))
                    .build();
                columns.put(
                    columnName,
                    domainProperty
                );
            }

            JSObject rootObject = (JSObject) jsObject.getMember("rootObject");

            Object convertedRoot;

            int count = (int) jsObject.getMember("count");
            String qualifier = (String) jsObject.getMember("qualifier");

            if (rootObject.isArray())
            {
                convertedRoot = convertListfromJs(runtimeContext, columns, rootObject);
            }
            else
            {
                convertedRoot = convertMapFromJs(runtimeContext, columns, rootObject);
            }
            return new DataGraph(columns, convertedRoot, count, qualifier);
        }
        else
        {
            throw new IllegalArgumentException("Invalid datagraph value: " + value);
        }
    }




    @Override
    public DataGraph convertToJava(RuntimeContext runtimeContext, Map value)
    {
        Map<String, DomainProperty> columns = convertColumns((Map<String, Object>) value.get("columns"));

        Object rootObject = value.get("rootObject");

        int count = (int) value.get("count");
        String qualifier = (String) value.get("qualifier");

        return new DataGraph(columns, convertDataGraphRoot(runtimeContext, columns, rootObject), count, qualifier);
    }


    public static Map<String, DomainProperty> convertColumns(Map<String, Object> columnsIn)
    {
        Map<String, DomainProperty> columns = new HashMap<>();
        for (Map.Entry<String, Object> columnEntry : columnsIn.entrySet())
        {
            final DomainProperty domainProperty = DomainProperty.builder()
                .fromMap((Map<String, Object>) columnEntry.getValue())
                .build();

            columns.put(
                columnEntry.getKey(),
                domainProperty
            );
        }
        return columns;
    }


    public static Object convertDataGraphRoot(
        RuntimeContext runtimeContext,
        Map<String, DomainProperty> columns,
        Object rootObject
    )
    {

        if (rootObject instanceof Collection)
        {
            return convertList(runtimeContext, columns, (Collection<Map<String,Object>>) rootObject, true);
        }
        else
        {
            return convertMap(runtimeContext, columns, (Map<String,Object>)rootObject, true);
        }
    }


    @Override
    public Map convertToJSON(RuntimeContext runtimeContext, DataGraph value)
    {

        int count = value.getCount();
        String qualifier = value.getQualifier();

        final Map<String, DomainProperty> columns = value.getColumns();

        Object rootObject;
        if (value.isMap())
        {
            rootObject = convertMap(runtimeContext, columns, (Map<String,Object>)value.getRootObject(), false);
        }
        else
        {
            rootObject = convertList(runtimeContext, columns, (Collection<Map<String,Object>>)value.getRootCollection(), false);
        }
        final Map<String,Object> map = new HashMap<>();
        map.put("columns", columns);
        map.put("rootObject", rootObject);
        map.put("count", count);
        map.put("qualifier", qualifier);
        return map;
    }

    @Override
    public Class<DataGraph> getJavaType()
    {
        return DataGraph.class;
    }


    @Override
    public Class<Map> getJSONType()
    {
        return Map.class;
    }

    private static List<Map<String, Object>> convertList(
        RuntimeContext runtimeContext, Map<String, DomainProperty> columns, Collection<Map<String, Object>> collection,
        boolean convertToJava
    )
    {
        List<Map<String,Object>> list = new ArrayList<>(collection.size());

        for (Map<String, Object> row : collection)
        {
            Map<String,Object> convertedRow = Maps.newHashMapWithExpectedSize(row.size());

            for (DomainProperty domainProperty : columns.values())
            {
                final PropertyType propertyType = PropertyType.get(runtimeContext, domainProperty);

                final String propertyName = domainProperty.getName();
                if (convertToJava)
                {
                    convertedRow.put(propertyName, propertyType.convertToJava(runtimeContext, row.get(propertyName)));
                }
                else
                {
                    convertedRow.put(propertyName, propertyType.convertToJSON(runtimeContext, row.get(propertyName)));
                }
            }

            list.add(convertedRow);
        }
        return list;
    }

    private List<Map<String, Object>> convertListfromJs(
        RuntimeContext runtimeContext, Map<String, DomainProperty> columns, JSObject collection
    )
    {
        final int size = (int) collection.getMember("length");

        List<Map<String,Object>> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++)
        {
            final JSObject row = (JSObject) collection.getSlot(i);

            Map<String,Object> convertedRow = new HashMap<>();

            for (DomainProperty domainProperty : columns.values())
            {
                final String propertyName = domainProperty.getName();
                final PropertyType propertyType = PropertyType.get(
                    runtimeContext,
                    domainProperty
                );
                convertedRow.put(propertyName, propertyType.convertFromJs(runtimeContext, row.getMember(propertyName)));
            }

            list.add(convertedRow);
        }
        return list;
    }


    private static Map<String, Object> convertMap(
        RuntimeContext runtimeContext,
        Map<String, DomainProperty> columns,
        Map<String, Object> mapIn,
        boolean convertToJava
    )
    {
        Map<String,Object> map = Maps.newHashMapWithExpectedSize(mapIn.size());

        final DomainProperty wildCardColumn = columns.get(DataGraph.WILDCARD_SYMBOL);

        for (Map.Entry<String, Object> rootEntry : mapIn.entrySet())
        {
            final String columnName = rootEntry.getKey();
            Object columnValue = rootEntry.getValue();

            final DomainProperty domainProperty = wildCardColumn == null ?  columns.get(columnName) : wildCardColumn;

            final PropertyType propertyType = PropertyType.get(runtimeContext, domainProperty);

            if (convertToJava)
            {
                map.put(columnName, propertyType.convertToJava(runtimeContext, columnValue));
            }
            else
            {
                map.put(columnName, propertyType.convertToJSON(runtimeContext, columnValue));
            }
        }
        return map;
    }

    private Map<String,Object> convertMapFromJs(
        RuntimeContext runtimeContext, Map<String, DomainProperty> columns, JSObject rootObject
    )
    {
        Map<String,Object> map = new HashMap<>();

        final DomainProperty wildCardColumn = columns.get(DataGraph.WILDCARD_SYMBOL);


        for (String columnName : rootObject.keySet())
        {
            Object columnValue = rootObject.getMember(columnName);

            final DomainProperty domainProperty = wildCardColumn == null ?  columns.get(columnName) : wildCardColumn;

            final PropertyType propertyType = PropertyType.get(
                runtimeContext,
                domainProperty
            );
            map.put(columnName, propertyType.convertFromJs(runtimeContext, columnValue));
        }
        return map;
    }
}
