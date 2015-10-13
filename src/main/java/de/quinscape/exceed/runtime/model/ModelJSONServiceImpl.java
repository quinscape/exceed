package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSON;
import org.svenson.JSONCharacterSink;
import org.svenson.JSONParser;
import org.svenson.SinkAwareJSONifier;
import org.svenson.matcher.SubtypeMatcher;

public class ModelJSONServiceImpl
    implements ModelJSONService
{
    private static Logger log = LoggerFactory.getLogger(ModelJSONServiceImpl.class);


    private final JSONParser parser;
    private final JSON generator;
    private final JSON externalGenerator;

    public ModelJSONServiceImpl(ModelFactory modelFactory)
    {
        parser = new JSONParser();
        parser.setTypeMapper(new ModelMapper());
        if (modelFactory != null)
        {
            parser.addObjectFactory(modelFactory);
        }

        generator = new JSON();
        generator.registerJSONifier(Attributes .class, new AttributesJSONifier(false));

        externalGenerator = new JSON();
        externalGenerator.registerJSONifier(Attributes .class, new AttributesJSONifier(true));
    }

    @Override
    public String toJSON(Object model)
    {
        return generator.forValue(model);
    }


    @Override
    public String toExternalJSON(Object model)
    {
        return externalGenerator.forValue(model);
    }

    /**
     * Converts the given model JSON to a model instance.
     *
     * @param json      JSON string. Must have a root "_type" property that contains a valid model name,
     *
     * @return Model instance of type embedded in JSON
     */
    @Override
    public Model toModel(String json)
    {
        return parser.parse(Model.class, json);
    }

    /**
     * Converts the given model JSON to a model instance and validates it to be an expected type.
     *
     * @param cls       Expected (super) class.
     * @param json      JSON string. Must have a root "_type" property that contains a valid model name,
     *
     * @return Model instance of type embedded in JSON
     */
    @Override
    public <M extends Model> M toModel(Class<M> cls, String json) throws IllegalArgumentException
    {
        Model model = parser.parse(cls, json);
        if (!cls.isInstance(model))
        {
            throw new IllegalArgumentException("Expected " + cls.getSimpleName() + " but got " + json);
        }
        return (M)model;
    }

    public static class ModelMapper extends AbstractPropertyValueBasedTypeMapper
    {
        public ModelMapper()
        {
            setDiscriminatorField("_type");
            setPathMatcher(new SubtypeMatcher(Model.class));
        }

        @Override
        protected Class getTypeHintFromTypeProperty(Object o) throws IllegalStateException
        {
            if (o == null)
            {
                return null;
            }

            try
            {
                return Class.forName(MODEL_PACKAGE + "." + o);
            }
            catch (ClassNotFoundException e)
            {
                throw new ExceedRuntimeException(e);
            }
        }
    }

    /**
     * Returns a type string for a model class.
     *
     * @param cls   model class
     * @return type string used by domain object "_type" fields.
     */
    public static String getType(Class<? extends Model> cls)
    {
        String className = cls.getName();

        if (!className.startsWith(MODEL_PACKAGE) || className.charAt(MODEL_PACKAGE.length()) != '.')
        {
            throw new IllegalArgumentException(cls + " is not in package " + MODEL_PACKAGE);
        }

        return className.substring(MODEL_PACKAGE.length() + 1);
    }

    private class AttributesJSONifier
        implements SinkAwareJSONifier
    {
        private final boolean ignoreGeneratedIds;

        private AttributesJSONifier(boolean ignoreGeneratedIds)
        {
            this.ignoreGeneratedIds = ignoreGeneratedIds;
        }

        @Override
        public void writeToSink(JSONCharacterSink sink, Object o)
        {
            Attributes  attrs = (Attributes)o;

            sink.append('{');
            boolean first = true;
            for (String name : attrs.getNames())
            {
                if (!ignoreGeneratedIds || !name.equals("id") || !attrs.isIdGenerated() )
                {
                    if (!first)
                    {
                        sink.append(',');
                    }
                    generator.quote(sink, name);
                    sink.append(':');
                    generator.dumpObject(sink, attrs.getAttribute(name).getValue());

                    first = false;
                }
            }
            sink.append('}');
        }

        @Override
        public String toJSON(Object o)
        {
            throw new UnsupportedOperationException();
        }
    }
}