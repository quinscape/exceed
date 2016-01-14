package de.quinscape.exceed.runtime.datalist;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.expression.query.DataField;
import org.svenson.JSON;
import org.svenson.JSONCharacterSink;
import org.svenson.JSONParser;
import org.svenson.SinkAwareJSONifier;
import org.svenson.util.JSONBeanUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class DataListService
{
    private final Map<String, PropertyConverter> propertyConverters;

    private JSON generator = JSON.defaultJSON();

    private JSONParser parser;

    private final static JSONBeanUtil util = JSONBeanUtil.defaultUtil();


    public DataListService(Map<String, PropertyConverter> propertyConverters)
    {
        this.propertyConverters = ImmutableMap.copyOf(propertyConverters);

        generator = new JSON();
        generator.registerJSONifier(DataList.class, new DataListJSONifier());
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

            sink.append("{\"types\":");

            generator.dumpObject(sink, dataList.getTypes());

            sink.append(",\"columns\":{");

            Map<String, DataField> fields = dataList.getFields();
            for (Iterator<Map.Entry<String, DataField>> iterator = fields.entrySet().iterator(); iterator.hasNext(); )
            {
                Map.Entry<String, DataField> entry = iterator.next();

                String localName = entry.getKey();
                DataField dataField = entry.getValue();

                generator.quote(sink, localName);
                DomainProperty domainProperty = dataField.getDomainProperty();
                sink.append(":{\"domainType\":");
                generator.quote(sink, dataField.getQueryDomainType().getType().getName());
                sink.append(",\"name\":");
                generator.dumpObject(sink, domainProperty.getName());
                sink.append("}");

                if (iterator.hasNext())
                {
                    sink.append(",");
                }
            }

            sink.append("},\"rows\":[");

            for (Iterator<?> iterator = dataList.getRows().iterator(); iterator.hasNext(); )
            {
                Object row = iterator.next();

                jsonifyRow(sink, runtimeContext, dataList, row);

                if (iterator.hasNext())
                {
                    sink.append(",");
                }
            }

            sink.append("]}");
        }


        private void jsonifyRow(JSONCharacterSink sink, RuntimeContext runtimeContext, DataList dataList,
                                Object row)
        {
            sink.append("{\"id\":");

            generator.dumpObject(sink, util.getProperty(row, "id"));

            Collection<DataField> dataFields = dataList.getFields().values();

            for (DataField field : dataFields)
            {
                String localName = field.getLocalName();

                if (localName.equals("id"))
                {
                    continue;
                }

                Object property = util.getProperty(row, localName);

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
