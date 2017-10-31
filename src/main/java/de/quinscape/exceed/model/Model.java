package de.quinscape.exceed.model;

import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import org.svenson.JSONProperty;

public interface Model
{
    String TYPE_NAME_PREFIX = "xcd.";
    /**
     * Base package for all model classes.
     */
    String MODEL_PACKAGE = Model.class.getPackage().getName();

    /**
     * Returns a type string for a model class.
     *
     * @param cls model class
     * @return type string used by domain object "type" fields.
     *
     * @throws IllegalArgumentException if the given class is invalid, i.e. is not in {@link #MODEL_PACKAGE}
     */
    static String getType(Class<?> cls)
    {
        final String type = findType(cls);
        if (type == null)
        {
            throw new IllegalArgumentException(cls + " is not in package " + MODEL_PACKAGE);
        }
        return type;
    }

    /**
     * Returns a model class for a type string.
     *
     * @param type      type string (e.g. "xcd.view.View")
     * @return type string used by domain object "type" fields.
     */
    static <T extends Model> Class<T> getType(String type)
    {
        if (!type.startsWith(TYPE_NAME_PREFIX))
        {
            throw new IllegalArgumentException("Invalid type: " + type);
        }

        try
        {
            return (Class<T>) Class.forName(MODEL_PACKAGE + "." + type.substring(TYPE_NAME_PREFIX.length()));
        }
        catch(ClassNotFoundException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    /**
     * Finds the type string for the given type class.
     *
     * @param cls       type class
     * @return  type or <code>null</code>
     */
    static String findType(Class<?> cls)
    {
        String className = cls.getName();

        if (!className.startsWith(MODEL_PACKAGE) || className.charAt(MODEL_PACKAGE.length()) != '.')
        {
            return null;
        }

        return TYPE_NAME_PREFIX + className.substring(MODEL_PACKAGE.length() + 1);
    }

    @JSONProperty(value = "type", readOnly = true, priority = 90)
    @Internal
    String getType();

    /**
     * Arbitrary JSON user data field. Not used by the system. Can be used for user annotate meta data annotation.
     */
    @JSONProperty(value = "_", ignoreIfNull = true, priority = 100)
    @Internal
    Object getAnnotation();

    void setAnnotation(Object annotation);
}
