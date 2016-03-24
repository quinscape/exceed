package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;

public class ModelJSONServiceImpl
    implements ModelJSONService
{
    /**
     * Base package for all model classes.
     */
    public final static String MODEL_PACKAGE = Model.class.getPackage().getName();

    public static final String CONTEXT_DEFAULT_NAME = "context";

    public static final String MODEL_ATTR_NAME = "model";

    private final static Logger log = LoggerFactory.getLogger(ModelJSONServiceImpl.class);

    private final JSONParser parser;

    private final JSON generator = JSON.defaultJSON();

    private ClientViewJSONGenerator viewJSONGenerator = new ClientViewJSONGenerator();


    public ModelJSONServiceImpl()
    {
        parser = new JSONParser();
        parser.setTypeMapper(new ModelMapper());
    }


    @Override
    public String toJSON(Object model)
    {
        if (model instanceof View)
        {
            return toJSON(null, (View) model, JSONFormat.EXTERNAL);
        }
        return generator.forValue(model);
    }

    @Override
    public String toJSON(RuntimeApplication application, View model, JSONFormat jsonFormat)
    {
        if (jsonFormat == null)
        {
            throw new IllegalArgumentException("jsonFormat can't be null");
        }

        return viewJSONGenerator.toJSON(application, model, jsonFormat);
    }


    /**
     * Converts the given model JSON to a model instance and validates it to be an expected type.
     *
     * @param cls  Expected (super) class.
     * @param json JSON string. Must have a root "type" property that contains a valid model name,
     * @return Model instance of type embedded in JSON
     */
    @Override
    public <M> M toModel(Class<M> cls, String json) throws IllegalArgumentException
    {

        try
        {
            Object model = parser.parse((Class)cls, json);
            if (!cls.isInstance(model))
            {
                throw new IllegalArgumentException("Expected " + cls.getSimpleName() + " but got " + json);
            }
            return (M) model;
        }
        catch (IllegalArgumentException e)
        {
            throw new ExceedRuntimeException("Error converting model JSON" + json, e);
        }
    }


    public static class ModelMapper
        extends AbstractPropertyValueBasedTypeMapper
    {
        public ModelMapper()
        {
            setDiscriminatorField("type");
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
     * @param cls model class
     * @return type string used by domain object "type" fields.
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
