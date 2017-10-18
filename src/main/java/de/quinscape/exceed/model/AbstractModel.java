package de.quinscape.exceed.model;

import de.quinscape.exceed.model.annotation.Internal;
import org.svenson.JSONProperty;

/**
 * Base class for model classes.
 *
 * Classes that can exist on their own need this base class, sub-structures of these classes can use this
 * base class but don't really need to since the surrounding model class provides the necessary type hints.
 *
 */
public abstract class AbstractModel
    implements Model
{

    private Object annotation;

    private final String type = Model.getType(this.getClass());

    /**
     * Provides a read-only "type" property with the path relative to this package which is used by {@link de.quinscape.exceed.runtime.model.ModelJSONServiceImpl}
     * to parse JSON into the correct java type.
     *
     * @return
     */
    @Override
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
    @Override
    @JSONProperty(value = "_", ignoreIfNull = true, priority = 100)
    @Internal
    public Object getAnnotation()
    {
        return annotation;
    }


    @Override
    public void setAnnotation(Object annotation)
    {
        this.annotation = annotation;
    }


    @Override
    public String toString()
    {
        return super.toString() + (annotation != null ? ": ( " + annotation + " )" : "");
    }


}
