package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.model.ModelBase;
import org.svenson.AbstractPropertyValueBasedTypeMapper;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;

public class ModelService
{
    public final static String PACKAGE = ModelBase.class.getPackage().getName();

    private JSONParser parser;

    public ModelService()
    {
        parser = new JSONParser();
        parser.setTypeMapper(new ModelMapper());
    }

    public <M extends ModelBase> String toJSON(M model)
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
    public ModelBase toModel(String json)
    {
        return parser.parse(ModelBase.class, json);
    }

    /**
     * Converts the given model JSON to a model instance and validates it to be an expected type.
     *
     * @param cls       Expected (super) class.
     * @param json      JSON string. Must have a root "_type" property that contains a valid model name,
     *
     * @return Model instance of type embedded in JSON
     */
    public <M extends ModelBase> M toModel(Class<M> cls, String json)
    {
        ModelBase model = parser.parse(ModelBase.class, json);
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
            setPathMatcher(new SubtypeMatcher(ModelBase.class));
        }

        @Override
        protected Class getTypeHintFromTypeProperty(Object o) throws IllegalStateException
        {
            try
            {
                return Class.forName(PACKAGE + "." + o);
            }
            catch (ClassNotFoundException e)
            {
                throw new ExceedRuntimeException(e);
            }
        }
    }

    public static String getType(Class<? extends ModelBase> cls)
    {
        String className = cls.getName();

        if (!className.startsWith(PACKAGE) || className.charAt(PACKAGE.length()) != '.')
        {
            throw new IllegalArgumentException(cls + " is not in package " + PACKAGE);
        }

        return className.substring(PACKAGE.length() + 1);
    }
}


