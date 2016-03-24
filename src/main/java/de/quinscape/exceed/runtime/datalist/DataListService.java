package de.quinscape.exceed.runtime.datalist;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.component.ColumnDescriptor;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import org.svenson.JSON;
import org.svenson.JSONCharacterSink;
import org.svenson.JSONParser;
import org.svenson.JSONifier;
import org.svenson.SinkAwareJSONifier;
import org.svenson.util.JSONBeanUtil;
import org.svenson.util.JSONBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataListService
{
    private final Map<String, PropertyConverter> propertyConverters;

    private final DataListJSONifier jsonifier;

    private JSON generator = JSON.defaultJSON();

    private JSONParser parser;

    private final static JSONBeanUtil util = JSONBeanUtil.defaultUtil();


    public DataListService(Map<String, PropertyConverter> propertyConverters)
    {
        this.propertyConverters = ImmutableMap.copyOf(propertyConverters);

        generator = new JSON();
        jsonifier = new DataListJSONifier();
        generator.registerJSONifier(DataList.class, jsonifier);
    }


    public String toJSON(Object domainObject)
    {
        return generator.forValue(domainObject);
    }


    /**
     * JSONifies QueryResult classes with entity definitions, query fields and rows.
     * <p>
     * Converts the row values using the RuntimeContextHolder to get the current runtime
     * context.
     */
    private class DataListJSONifier
        implements SinkAwareJSONifier
    {

        private static final String PROPERTY_COMPLEX_NAME = "value";


        @Override
        public String toJSON(Object o)
        {
            throw new UnsupportedOperationException();
        }


        @Override
        public void writeToSink(JSONCharacterSink sink, Object o)
        {
            RuntimeContext runtimeContext = RuntimeContextHolder.get();

            DataList dataList = (DataList) o;


            JSONBuilder b = JSONBuilder.buildObject(generator, sink);

            b.property("types", dataList.getTypes());
            b.property("enums", dataList.getEnums());
            b.property("columns", dataList.getColumns());
            b.arrayProperty("rows");

            Map<String, PropertyLookup> lookups = lookupConverters(dataList);

            for (Iterator<?> iterator = dataList.getRows().iterator(); iterator.hasNext(); )
            {
                Object row = iterator.next();
                convertRow(b, runtimeContext, dataList, row, lookups);
            }

            b.close();

            b.property("rowCount", dataList.getRowCount());

            b.closeAll();
        }


        private Map<String, PropertyLookup> lookupConverters(DataList dataList)
        {
            Map<String, PropertyLookup> converters = new HashMap<>();
            for (Map.Entry<String, ColumnDescriptor> entry : dataList.getColumns().entrySet())
            {
                ColumnDescriptor descriptor = entry.getValue();
                String columnName = descriptor.getName();

                DomainType type = dataList.getTypes().get(descriptor.getType());

                PropertyLookup lookup = null;

                if (type.getPkFields().contains(columnName))
                {
                    // TODO: non-string PKs
                    lookup = new PropertyLookup((PropertyConverter) converters.get("UUIDConverter"));
                }
                else
                {
                    List<DomainProperty> properties = type.getProperties();
                    for (DomainProperty property : properties)
                    {
                        if (property.getName().equals(columnName))
                        {
                            lookup = createLookup(dataList, property);
                            break;
                        }
                    }
                }

                if (lookup == null)
                {
                    throw new IllegalStateException(descriptor.getType() + " has no property descriptor for '"
                        + columnName + "'");
                }

                converters.put(entry.getKey(), lookup);
            }
            return converters;
        }


        private PropertyLookup createLookup(DataList dataList, DomainProperty property)
        {
            PropertyLookup lookup;
            String type = property.getType();

            if (isComplexType(type))
            {
                String name = (String) property.getTypeParam();

                DomainType domainType = dataList.getTypes().get(name);
                if (domainType != null)
                {
                    lookup = new PropertyLookup(property, null, createSubLookup(dataList, domainType), false);
                }
                else
                {
                    PropertyConverter propertyConverter = propertyConverters.get(getConverterName(name));
                    if (propertyConverter != null)
                    {
                        lookup = new PropertyLookup(property, null, ImmutableMap.of(PROPERTY_COMPLEX_NAME, new
                            PropertyLookup(new DomainProperty(type + "Property", name, null, false),
                            propertyConverter)), true);
                    }
                    else
                    { // list of simple properties
                        throw new IllegalStateException("Cannot find domain type or property type for complex " +
                            "property " + property + " in  " + dataList);
                    }
                }
            }
            else
            {
                String converterName = getConverterName(type);
                PropertyConverter converter = propertyConverters.get(converterName);

                if (converter == null)
                {
                    throw new IllegalStateException("Cannot find converter '" + converterName + "'");
                }

                lookup = new PropertyLookup(property, converter);
            }
            return lookup;
        }


        private Map<String, PropertyLookup> createSubLookup(DataList dataList, DomainType property)
        {
            HashMap<String, PropertyLookup> map = new HashMap<>();
            for (DomainProperty domainProperty : property.getProperties())
            {
                map.put(domainProperty.getName(), createLookup(dataList, domainProperty));
            }
            return map;
        }


        private void convertRow(JSONBuilder b, RuntimeContext runtimeContext, DataList dataList,
                                Object row, Map<String, PropertyLookup> lookups)
        {
            b.objectElement();

            for (Map.Entry<String, PropertyLookup> entry : lookups.entrySet())
            {
                String localName = entry.getKey();
                PropertyLookup lookup = entry.getValue();

                Object value = util.getProperty(row, localName);
                if (lookup.isPKField())
                {
                    b.property(localName, value);
                }
                else
                {
                    convertValue(b, runtimeContext, dataList, localName, lookup, value);
                }

            }

            b.close();
        }


        private void convertValue(JSONBuilder b, RuntimeContext runtimeContext, DataList dataList, String localName,
                                  PropertyLookup
            lookup, Object value)
        {

            if (lookup.getPropertyConverter() == null)
            {

                DomainProperty domainProperty = lookup.getDomainProperty();
                String domainPropertyType = domainProperty.getType();
                switch (domainPropertyType)
                {
                    case "List":
                    {
                        List list = (List) value;

                        if (localName != null)
                        {
                            b.arrayProperty(localName);
                        }
                        else
                        {
                            b.arrayElement();
                        }

                        for (Object o : list)
                        {
                            Map<String, PropertyLookup> subLookup = lookup.getSubLookup();

                            if (lookup.isPropertyComplexValue())
                            {
                                convertValue(b, runtimeContext, dataList, null, subLookup.get(PROPERTY_COMPLEX_NAME),
                                    o);
                            }
                            else
                            {
                                b.objectElement();
                                convertInnerObject(b, runtimeContext, dataList, o, subLookup);
                                b.close();
                            }
                        }
                        b.close();
                        break;
                    }
                    case "Map":
                    {
                        Map<String, Object> map = (Map) value;

                        if (localName != null)
                        {
                            b.objectProperty(localName);
                        }
                        else
                        {
                            b.objectElement();
                        }

                        for (Map.Entry<String, Object> mapEntry : map.entrySet())
                        {
                            String mapKey = mapEntry.getKey();
                            Object mapValue = mapEntry.getValue();

                            Map<String, PropertyLookup> subLookup = lookup.getSubLookup();


                            if (lookup.isPropertyComplexValue())
                            {
                                convertValue(b, runtimeContext, dataList, mapKey, subLookup.get
                                    (PROPERTY_COMPLEX_NAME), mapValue);
                            }
                            else
                            {
                                b.objectProperty(mapKey);
                                convertInnerObject(b, runtimeContext, dataList, mapValue, subLookup);
                                b.close();
                            }

                        }
                        b.close();
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unknown domain property type '" + domainPropertyType + "'");
                }
            }
            else
            {
                Object converted = lookup.convert(runtimeContext, value);
                if (localName != null)
                {
                    b.property(localName, converted);
                }
                else
                {
                    b.element(converted);
                }
            }
        }


        private void convertInnerObject(JSONBuilder b, RuntimeContext runtimeContext, DataList dataList, Object o,
                                        Map<String, PropertyLookup> subLookup)
        {
            for (Map.Entry<String, PropertyLookup> e : subLookup.entrySet())
            {
                String name = e.getKey();
                PropertyLookup propertyLookup = e.getValue();
                Object propertyValue = util.getProperty(o, name);
                convertValue(b, runtimeContext, dataList, name, propertyLookup, propertyValue);
            }
        }
    }


    private String getConverterName(String type)
    {
        return type + "Converter";
    }


    private boolean isComplexType(String type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }


        return type.equals("List") || type.equals("Map");
    }


    public JSONifier getJSONifier()
    {
        return jsonifier;
    }


    private class PropertyLookup
    {
        private final DomainProperty domainProperty;

        private final PropertyConverter propertyConverter;

        private final Map<String, PropertyLookup> subLookup;

        private final boolean propertyComplexValue;


        public PropertyLookup(PropertyConverter propertyConverter)
        {
            this(null, propertyConverter, null);
        }

        public PropertyLookup(DomainProperty domainProperty, PropertyConverter propertyConverter)
        {
            this(domainProperty, propertyConverter, null);
        }


        public PropertyLookup(DomainProperty domainProperty, PropertyConverter propertyConverter, Map<String,
            PropertyLookup> subLookup)
        {
            this(domainProperty, propertyConverter, subLookup, false);
        }


        public PropertyLookup(DomainProperty domainProperty, PropertyConverter propertyConverter, Map<String,
            PropertyLookup> subLookup, boolean propertyComplexValue)
        {
            this.domainProperty = domainProperty;
            this.propertyConverter = propertyConverter;
            this.subLookup = subLookup;
            this.propertyComplexValue = propertyComplexValue;
        }


        public DomainProperty getDomainProperty()
        {
            return domainProperty;
        }


        public PropertyConverter getPropertyConverter()
        {
            return propertyConverter;
        }


        public Map<String, PropertyLookup> getSubLookup()
        {
            return subLookup;
        }


        public Object convert(RuntimeContext runtimeContext, Object value)
        {
            return propertyConverter.convertToJSON(runtimeContext, value, domainProperty);
        }


        public boolean isPropertyComplexValue()
        {
            return propertyComplexValue;
        }

        public boolean isPKField()
        {
            return domainProperty == null;

        }
    }
}
