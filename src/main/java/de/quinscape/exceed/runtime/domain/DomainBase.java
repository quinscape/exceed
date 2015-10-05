// THIS CLASS GETS DELETED DURING THE JOOQ CODE GENERATION AND THEN RESTORED from src/main/resources/DomainBase.java.
package de.quinscape.exceed.runtime.domain;

import org.svenson.JSON;
import org.svenson.JSONProperty;

/**
 * Base class for model classes.
 *
 * Classes that can exist on their own need this base class, sub-structures of these classes can use this
 * base class but don't really need to since the surrounding model class provides the necessary type hints.
 *
 * Optimally, this class should be in {@link de.quinscape.exceed.domain} but JOOQ keeps deleting it, so we keep
 * it here.
 *
 */
public abstract class DomainBase
{
    private String id;

    @JSONProperty(value = "_type", readOnly = true, priority = 100)
    public String getType()
    {
        return this.getClass().getSimpleName();
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

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
