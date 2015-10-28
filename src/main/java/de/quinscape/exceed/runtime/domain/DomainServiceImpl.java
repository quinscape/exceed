package de.quinscape.exceed.runtime.domain;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.component.IdentityDefinition;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.expression.query.DataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;
import org.svenson.JSONCharacterSink;
import org.svenson.JSONParser;
import org.svenson.SinkAwareJSONifier;
import org.svenson.util.JSONBeanUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DomainServiceImpl
    implements DomainService
{
    private static Logger log = LoggerFactory.getLogger(DomainServiceImpl.class);

    private final static JSONBeanUtil util = JSONBeanUtil.defaultUtil();

    private final Map<String, PropertyConverter> propertyConverters;

    private JSONParser parser;

    private JSON generator = JSON.defaultJSON();

    private String schema;

    private RuntimeApplication runtimeApplication;


    public DomainServiceImpl(Map<String, PropertyConverter> propertyConverters)
    {

        parser = new JSONParser();
        parser.addObjectFactory(new DomainFactory(this));

        generator = new JSON();
        generator.registerJSONifier(DataList.class, new DataListJSONifier());

        this.propertyConverters = ImmutableMap.copyOf(propertyConverters);
    }


    public void init(RuntimeApplication runtimeApplication, String schema)
    {
        this.runtimeApplication = runtimeApplication;
        this.schema = schema;
    }


    @Override
    public String toJSON(RuntimeContext runtimeContext, Object domainObject)
    {
        return generator.forValue(domainObject);
    }


    /**
     * Converts the given model JSON to a model instance.
     *
     * @param json JSON string. Must have a root "_type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public Object toDomainObject(RuntimeContext runtimeContext, String json)
    {
        return parser.parse(GenericDomainObject.class, json);
    }


    @Override
    public DomainType getDomainType(String name)
    {
        DomainType domainType = runtimeApplication.getApplicationModel().getDomainTypes().get(name);
        if (domainType == null)
        {
            throw new IllegalArgumentException("Unknown type '" + name + "'");
        }

        return domainType;
    }


    @Override
    public String getSchema()
    {
        return schema;
    }


    public Map<String, DomainType> getDomainTypes()
    {
        return runtimeApplication.getApplicationModel().getDomainTypes();
    }


    private class DomainFactory
        implements org.svenson.ObjectFactory<GenericDomainObject>
    {
        private final DomainService domainService;


        private DomainFactory(DomainService domainService)
        {
            if (domainService == null)
            {
                throw new IllegalArgumentException("domainService can't be null");
            }

            this.domainService = domainService;
        }


        @Override
        public boolean supports(Class<GenericDomainObject> cls)
        {
            return GenericDomainObject.class.isAssignableFrom(cls);
        }


        @Override
        public GenericDomainObject create(Class<GenericDomainObject> cls)
        {
            try
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Create {}", cls.getName());
                }
                GenericDomainObject domainObject = cls.newInstance();

                domainObject.setDomainService(domainService);
                return domainObject;
            }
            catch (Exception e)
            {
                throw new DomainObjectCreationException("Error creating domain object", e);
            }
        }
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

            List<IdentityDefinition> identityDefinitions = dataList.getEntityDefinitions();
            Map<String, Integer> lookup = new HashMap<>();
            for (int i = 0; i < identityDefinitions.size(); i++)
            {
                IdentityDefinition identityDefinition = identityDefinitions.get(i);
                lookup.put(identityDefinition.getType(), i);
            }

            sink.append("{\"entities\":");

            generator.dumpObject(sink, identityDefinitions);

            sink.append(",\"fields\":{");

            Map<String, DataField> fields = dataList.getFields();
            for (Iterator<Map.Entry<String, DataField>> iterator = fields.entrySet().iterator(); iterator.hasNext(); )
            {
                Map.Entry<String, DataField> entry = iterator.next();

                String localeName = entry.getKey();
                DataField dataField = entry.getValue();

                jsonifyQueryField(sink, localeName, dataField, lookup);

                if (iterator.hasNext())
                {
                    sink.append(",");
                }
            }

            sink.append("},\"rows\":[");

            for (Iterator<? extends DomainObject> iterator = dataList.getRows().iterator(); iterator.hasNext(); )
            {
                DomainObject row = iterator.next();

                jsonifyRow(sink, runtimeContext, dataList, row);

                if (iterator.hasNext())
                {
                    sink.append(",");
                }
            }

            sink.append("]}");
        }


        private void jsonifyQueryField(JSONCharacterSink sink, String localeName, DataField dataField, Map<String,
            Integer> lookup)
        {
            generator.quote(sink, localeName);
            DomainProperty domainProperty = dataField.getDomainProperty();
            sink.append(":{\"type\":");
            generator.quote(sink, domainProperty.getType());
            Object typeParam = domainProperty.getTypeParam();
            if (typeParam != null)
            {
                sink.append(",\"typeParam\":");
                generator.dumpObject(sink, typeParam);
            }
            boolean required = domainProperty.isRequired();
            if (required)
            {
                sink.append(",\"required\":true");
            }
            boolean translationNeeded = domainProperty.isTranslationNeeded();
            if (translationNeeded)
            {
                sink.append(",\"translationNeeded\":true");
            }
            int maxLength = domainProperty.getMaxLength();
            if (maxLength > 0)
            {
                sink.append(",\"maxLength\":" + maxLength);
            }
            String defaultValue = domainProperty.getDefaultValue();
            if (defaultValue != null)
            {
                sink.append(",\"defaultValue\":");
                generator.dumpObject(sink, defaultValue);
            }
            sink.append(",\"entityIndex\":" + lookup.get(dataField.getQueryDomainType().getType().getName()));
            sink.append(",\"name\":");
            generator.quote(sink, dataField.getDomainProperty().getName());
            sink.append(",\"qualifiedName\":");
            generator.quote(sink, dataField.getQualifiedName());
            sink.append("}");
        }


        private void jsonifyRow(JSONCharacterSink sink, RuntimeContext runtimeContext, DataList dataList,
                                DomainObject row)
        {
            sink.append("{\"id\":");
            generator.quote(sink, row.getId());

            Collection<DataField> dataFields = dataList.getFields().values();

            for (DataField field : dataFields)
            {
                String localName = field.getLocalName();
                Object property = row.getProperty(localName);

                DomainProperty domainProperty = field.getDomainProperty();
                String converterBeanName = domainProperty.getType() + "Converter";
                PropertyConverter converter = propertyConverters.get(converterBeanName);
                if (converter == null)
                {
                    throw new IllegalStateException("Could not find converter '" + converterBeanName + "'");
                }

                Object converted = converter.convertToJSON(runtimeContext, property, domainProperty);

                sink.append(",");
                generator.quote(sink, localName);
                sink.append(":");
                generator.dumpObject(sink, converted);
            }

            sink.append("}");
        }
    }
}
