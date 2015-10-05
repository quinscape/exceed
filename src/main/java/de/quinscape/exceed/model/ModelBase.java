package de.quinscape.exceed.model;

import de.quinscape.exceed.runtime.model.ModelService;
import org.svenson.JSON;
import org.svenson.JSONProperty;

/**
 * Base class for model classes.
 *
 * Classes that can exist on their own need this base class, sub-structures of these classes can use this
 * base class but don't really need to since the surrounding model class provides the necessary type hints.
 *
 */
public abstract class ModelBase
{

    private Object annotation;

    /**
     * Provides a read-only "_type" property with the path relative to this package which is used by {@link ModelService}
     * to parse JSON into the correct java type.
     *
     * @return
     */
    @JSONProperty(value = "_type", readOnly = true, priority = 100)
    public String getType()
    {
        return ModelService.getType(this.getClass());
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

    /**
     * Allows to specify arbitrary JSON content as annotation on a model instance. This can be just a description
     * thing or any other kind of JSONable meta data.
     *
     * @return
     */
    @JSONProperty(value = "_", ignoreIfNull = true, priority = 99)
    public Object getAnnotation()
    {
        return annotation;
    }

    public void setAnnotation(Object annotation)
    {
        this.annotation = annotation;
    }
}
