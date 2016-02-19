package de.quinscape.exceed.model;

import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import org.svenson.JSON;
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
    private Object annotation;


    /**
     * Provides a read-only "type" property with the path relative to this package which is used by {@link ModelJSONServiceImpl}
     * to parse JSON into the correct java type.
     *
     * @return
     */
    @JSONProperty(value = "type", readOnly = true, priority = 90)
    public String getType()
    {
        return ModelJSONServiceImpl.getType(this.getClass());
    }


    /**
     * Allows to specify arbitrary JSON content as annotation on a model instance. This can be just a description
     * thing or any other kind of JSONable meta data.
     *
     * @return
     */
    @JSONProperty(value = "_", ignoreIfNull = true, priority = 100)
    public Object getAnnotation()
    {
        return annotation;
    }


    public void setAnnotation(Object annotation)
    {
        this.annotation = annotation;
    }


    /**
     * JSONifies the current instance.
     *
     * @return JSON string
     */
    @Override
    public String toString()
    {
        return JSON.defaultJSON().forValue(this);
    }
}
