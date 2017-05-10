package de.quinscape.exceed.model;

import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import org.svenson.JSONProperty;

/**
 * Base class for model classes.
 *
 * Classes that can exist on their own need this base class, sub-structures of these classes can use this
 * base class but don't really need to since the surrounding model class provides the necessary type hints.
 *
 */
public abstract class Model
{
    public final static String TYPE_NAME_PREFIX = "xcd.";

    /**
     * Base package for all model classes.
     */
    public final static String MODEL_PACKAGE = Model.class.getPackage().getName();

    private Object annotation;

    private final String type = getType(this.getClass());

    /**
     * Provides a read-only "type" property with the path relative to this package which is used by {@link de.quinscape.exceed.runtime.model.ModelJSONServiceImpl}
     * to parse JSON into the correct java type.
     *
     * @return
     */
    @JSONProperty(value = "type", readOnly = true, priority = 90)
    @Internal
    public String getType()
    {
        return type;
    }


    /**
     * Allows to specify arbitrary JSON content as annotation on a model instance. This can be just a description
     * thing or any other kind of JSONable meta data.
     *
     * @return
     */
    @JSONProperty(value = "_", ignoreIfNull = true, priority = 100)
    @Internal
    public Object getAnnotation()
    {
        return annotation;
    }


    public void setAnnotation(Object annotation)
    {
        this.annotation = annotation;
    }


    @Override
    public String toString()
    {
        return super.toString() + (annotation != null ? ": ( " + annotation + " )" : "");
    }


    /**
     * Returns a type string for a model class.
     *
     * @param cls model class
     * @return type string used by domain object "type" fields.
     */
    public static String getType(Class<?> cls)
    {
        String className = cls.getName();

        if (!className.startsWith(MODEL_PACKAGE) || className.charAt(MODEL_PACKAGE.length()) != '.')
        {
            throw new IllegalArgumentException(cls + " is not in package " + MODEL_PACKAGE);
        }

        return TYPE_NAME_PREFIX + className.substring(MODEL_PACKAGE.length() + 1);
    }




    /**
     * Returns a model class for a type string.
     *
     * @param type      type string (e.g. "xcd.view.View")
     * @return type string used by domain object "type" fields.
     */
    public static <T extends Model> Class<T> getType(String type)
    {
        if (!type.startsWith(TYPE_NAME_PREFIX))
        {
            throw new IllegalArgumentException("Invalid type: " + type);
        }

        try
        {
            return (Class<T>) Class.forName(Model.MODEL_PACKAGE + "." + type.substring(TYPE_NAME_PREFIX.length()));
        }
        catch(ClassNotFoundException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

}
