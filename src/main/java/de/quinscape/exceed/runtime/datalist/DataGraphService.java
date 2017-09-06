package de.quinscape.exceed.runtime.datalist;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.js.env.SvensonJsAdapter;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
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

    private final DataGraphJSONifier jsonifier;

    private final DomainService domainService;

    private JSON generator;

    private final static JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

    public DataGraphService(DomainService domainService)
    {
        this.domainService = domainService;

        generator = new JSON();
        jsonifier = new DataGraphJSONifier();
        generator.registerJSONifier(DataGraph.class, jsonifier);
    }


    /**
     * Converts the given java bean graph to JSON, correctly handling contained data graph structures.
     *
     * <p>
     *     Note: THe DataGraphJSONifier used uses the runtime context registered for the current thread via {@link RuntimeContextHolder}.
     * </p>
     *
     * @param value
     * @return
     */
    public String toJSON(Object value)
    {
        try
        {
            return generator.forValue(value);
        }
        catch(Exception e)
        {
            throw new ExceedRuntimeException("Error generating JSON for " + value, e);
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
            if (runtimeContext == null)
            {
                throw new IllegalStateException("No runtime context set in RuntimeContextHolder");
            }

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
                        convertValue(builder, runtimeContext, name, property.getType(), property.getTypeParam(), value, PropertyType.get(runtimeContext, property));
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
                        convertValue(builder, runtimeContext, localName, domainProperty.getType(), domainProperty.getTypeParam(), value, PropertyType.get(runtimeContext, domainProperty));
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
                convertValue(b, runtimeContext, localName, domainProperty.getType(), domainProperty.getTypeParam(), value, PropertyType.get(runtimeContext, domainProperty));
            }
        }


        private void convertValue(JSONBuilder b, RuntimeContext runtimeContext, String localName, String domainPropertyType, Object domainPropertyTypeParam, Object value, PropertyType propertyType)
        {
            if (log.isDebugEnabled())
            {
                log.debug(
                    "Convert '{}' ( {}/{} ): value = {}",
                        localName,
                        domainPropertyType,
                        domainPropertyTypeParam,
                        value
                );
            }

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
                    case PropertyType.LIST:
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

                        String elemType = (String)domainPropertyTypeParam;
                        String elemTypeParam = null;
                        if (domainService.getDomainTypes().containsKey(elemType))
                        {
                            elemTypeParam = elemType;
                            elemType = PropertyType.DOMAIN_TYPE;
                        }

                        final PropertyType propType = runtimeContext.getApplicationModel().getMetaData().createPropertyType(elemType, elemTypeParam, null);
                        for (Object o : list)
                        {
                            convertValue(b, runtimeContext, null,  elemType, elemTypeParam, o, propType);
                        }
                        b.close();
                        break;
                    }
                    case PropertyType.MAP:
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

                        String objType = (String)domainPropertyTypeParam;

                        final PropertyModel collectionType = ExpressionUtil.getCollectionType(domainService, objType);
                        objType = collectionType.getType();
                        String objTypeParam = collectionType.getTypeParam();

                        final PropertyType propType = runtimeContext.getApplicationModel().getMetaData().createPropertyType(objType, objTypeParam, null);
                        for (Map.Entry<String, Object> mapEntry : map.entrySet())
                        {
                            String mapKey = mapEntry.getKey();
                            Object mapValue = mapEntry.getValue();


                            convertValue(b, runtimeContext, mapKey, objType, objTypeParam, mapValue, propType);
                        }
                        b.close();
                        break;
                    }
                    case PropertyType.DOMAIN_TYPE:
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
                            b.property(DomainType.TYPE_PROPERTY, domainObject.getDomainType());

                            final DomainType domainType = domainService.getDomainType(domainObject.getDomainType());


                            for (DomainProperty property : domainType.getProperties())
                            {
                                final String propertyName = property.getName();
                                convertValue(b, runtimeContext, propertyName, property.getType(), property.getTypeParam(), domainObject.getProperty(propertyName), PropertyType.get(runtimeContext, property));
                            }
                        }
                        else
                        {
                            final DomainType domainType = domainService.getDomainType((String) domainPropertyTypeParam);
                            b.property(DomainType.TYPE_PROPERTY, domainType.getName());

                            for (DomainProperty property : domainType.getProperties())
                            {
                                final String propertyName = property.getName();
                                convertValue(b, runtimeContext, propertyName, property.getType(), property.getTypeParam(), util.getProperty(value, propertyName), PropertyType.get(runtimeContext, property));
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
                if (propertyType == null)
                {
                    throw new ExceedRuntimeException("No runtime property type");
                }

                Object converted = propertyType.convertToJSON(runtimeContext, value);
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

    private boolean isComplexType(String type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        return type.equals(PropertyType.LIST) || type.equals(PropertyType.MAP) || type.equals(PropertyType.DOMAIN_TYPE);
    }


    public JSONifier getJSONifier()
    {
        return jsonifier;
    }

}
