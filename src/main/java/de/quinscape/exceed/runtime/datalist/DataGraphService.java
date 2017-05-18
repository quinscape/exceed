package de.quinscape.exceed.runtime.datalist;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;
import org.svenson.JSONCharacterSink;
import org.svenson.JSONifier;
import org.svenson.SinkAwareJSONifier;
import org.svenson.util.JSONBeanUtil;
import org.svenson.util.JSONBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DataGraphService
{
    private final static Logger log = LoggerFactory.getLogger(DataGraphService.class);


    private final Map<String, PropertyConverter> propertyConverters;

    private final DataGraphJSONifier jsonifier;

    private final DomainService domainService;

    private JSON generator = JSON.defaultJSON();

    private final static JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

    public DataGraphService(DomainService domainService, Map<String, PropertyConverter> propertyConverters)
    {
        this.domainService = domainService;
        this.propertyConverters = propertyConverters;

        generator = new JSON();
        jsonifier = new DataGraphJSONifier();
        generator.registerJSONifier(DataGraph.class, jsonifier);
    }
    

    public String toJSON(Object domainObject)
    {
        try
        {
            return generator.forValue(domainObject);
        }
        catch(Exception e)
        {
            throw new ExceedRuntimeException("Error generating JSON for " + domainObject, e);
        }
    }


    /**
     * JSONifies QueryResult classes with entity definitions, query fields and rows.
     * <p>
     * Converts the row values using the RuntimeContextHolder to get the current runtime
     * context.
     */
    private class DataGraphJSONifier
        implements SinkAwareJSONifier
    {

        @Override
        public String toJSON(Object o)
        {
            throw new UnsupportedOperationException();
        }


        @Override
        public void writeToSink(JSONCharacterSink sink, Object o)
        {
            RuntimeContext runtimeContext = RuntimeContextHolder.get();

            DataGraph dataGraph = (DataGraph) o;

            final Map<String, DomainProperty> columns = dataGraph.getColumns();

            JSONBuilder builder = JSONBuilder.buildObject(generator, sink);

            builder
                .property("type", dataGraph.getType())
                .propertyUnlessNull("qualifier", dataGraph.getQualifier())
                .property("columns", columns);

            final Object rootObject = dataGraph.getRootObject();

            if (rootObject instanceof Collection)
            {
                log.debug("Convert collection");

                builder.arrayProperty("rootObject");
                for (Object row : ((Collection) rootObject))
                {
                    builder.objectElement();
                    convertRow(builder, runtimeContext, dataGraph, row);
                    builder.close();
                }
                builder.close();
            }
            else if (rootObject instanceof Map)
            {
                log.debug("Convert map");

                Map<String,Object> map = (Map<String,Object>) rootObject;

                final DomainProperty property = dataGraph.getColumns().get(DataGraph.WILDCARD_SYMBOL);
                if (property != null)
                {
                    builder.objectProperty("rootObject");
                    for (Map.Entry<String,Object> e : map.entrySet())
                    {
                        String name = e.getKey();
                        Object value = e.getValue();
                        convertValue(builder, runtimeContext, name, property.getType(), property.getTypeParam(), value);
                    }
                    builder.close();
                }
                else
                {
                    builder.objectProperty("rootObject");
                    for (Map.Entry<String, DomainProperty> entry : dataGraph.getColumns()
                        .entrySet())
                    {
                        String localName = entry.getKey();
                        final DomainProperty domainProperty = entry.getValue();

                        final Object value = map.get(localName);
                        convertValue(builder, runtimeContext, localName, domainProperty.getType(), domainProperty.getTypeParam(), value);
                    }
                    builder.close();
                }
            }

            builder.property("count", dataGraph.getCount());

            builder.closeAll();
        }




        private void convertRow(JSONBuilder b, RuntimeContext runtimeContext, DataGraph dataGraph,
                                Object row)
        {
            final Map<String, DomainProperty> columns = dataGraph.getColumns();

            for (Map.Entry<String, DomainProperty> entry : columns.entrySet())
            {
                String localName = entry.getKey();
                DomainProperty domainProperty = entry.getValue();

                Object value = util.getProperty(row, localName);
                convertValue(b, runtimeContext, localName, domainProperty.getType(), domainProperty.getTypeParam(), value);
            }
        }


        private void convertValue(JSONBuilder b, RuntimeContext runtimeContext, String localName, String domainPropertyType, Object domainPropertyTypeName, Object value)
        {
            log.debug("Convert '{}' ( {}/{} ): value = {}", localName, domainPropertyType, domainPropertyTypeName, value);

            if (value == null)
            {
                if (localName != null)
                {
                    b.includeProperty(localName, "null");
                }
                else
                {
                    b.includeElement("null");
                }
            }
            else if (isComplexType(domainPropertyType))
            {
                switch (domainPropertyType)
                {
                    case DomainProperty.LIST_PROPERTY_TYPE:
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

                        String elemType = (String)domainPropertyTypeName;
                        String elemTypeParam = null;
                        if (domainService.getDomainTypes().containsKey(elemType))
                        {
                            elemTypeParam = elemType;
                            elemType = DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE;
                        }

                        for (Object o : list)
                        {
                            convertValue(b, runtimeContext, null,  elemType, elemTypeParam, o);
                        }
                        b.close();
                        break;
                    }
                    case DomainProperty.MAP_PROPERTY_TYPE:
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

                        String objType = (String)domainPropertyTypeName;
                        String objTypeParam = null;
                        if (domainService.getDomainTypes().containsKey(objType))
                        {
                            objTypeParam = objType;
                            objType = DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE;
                        }

                        for (Map.Entry<String, Object> mapEntry : map.entrySet())
                        {
                            String mapKey = mapEntry.getKey();
                            Object mapValue = mapEntry.getValue();

                            convertValue(b, runtimeContext, mapKey, objType, objTypeParam, mapValue);
                        }
                        b.close();
                        break;
                    }
                    case DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE:
                    {

                        if (localName != null)
                        {
                            b.objectProperty(localName);
                        }
                        else
                        {
                            b.objectElement();
                        }
                        if (value instanceof DomainObject)
                        {

                            DomainObject domainObject = (DomainObject) value;
                            b.property("_type", domainObject.getDomainType());

                            final DomainType domainType = domainService.getDomainType(domainObject.getDomainType());


                            for (DomainProperty property : domainType.getProperties())
                            {
                                final String propertyName = property.getName();
                                convertValue(b, runtimeContext, propertyName, property.getType(), property.getTypeParam(), domainObject.getProperty(propertyName));
                            }
                        }
                        else if (value instanceof Model)
                        {
                            Model model = (Model) value;

                            final DomainType domainType = domainService.getDomainType(model.getType());
                            b.property("_type", domainType.getName());

                            for (DomainProperty property : domainType.getProperties())
                            {
                                final String propertyName = property.getName();
                                convertValue(b, runtimeContext, propertyName, property.getType(), property.getTypeParam(), util.getProperty(model, propertyName));
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
                final String converterName = getConverterName(domainPropertyType);
                final PropertyConverter converter = propertyConverters.get(converterName);

                if (converter == null)
                {
                    throw new ExceedRuntimeException("Could not find converter for property type '" + domainPropertyType+ "'." );
                }

                Object converted = converter.convertToJSON(runtimeContext, value);
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

        return type.equals(DomainProperty.LIST_PROPERTY_TYPE) || type.equals(DomainProperty.MAP_PROPERTY_TYPE) || type.equals(DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE);
    }


    public JSONifier getJSONifier()
    {
        return jsonifier;
    }

}
