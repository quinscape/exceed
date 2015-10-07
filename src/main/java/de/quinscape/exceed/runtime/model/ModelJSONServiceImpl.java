package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;

import java.util.concurrent.atomic.AtomicLong;

public class ModelJSONServiceImpl
    implements ModelJSONService
{
    private static Logger log = LoggerFactory.getLogger(ModelJSONServiceImpl.class);

    private JSONParser parser;

    private final static AtomicLong idCounter = new AtomicLong(0L);

    public ModelJSONServiceImpl(ModelFactory modelFactory)
    {
        parser = new JSONParser();
        parser.setTypeMapper(new ModelMapper());
        if (modelFactory != null)
        {
            parser.addObjectFactory(modelFactory);
        }
    }

    @Override
    public <M extends Model> String toJSON(M model)
    {
        return JSON.defaultJSON().forValue(model);
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

}
