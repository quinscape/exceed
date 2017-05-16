package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.util.JSONUtil;
import org.svenson.JSONProperty;
import org.svenson.util.JSONBeanUtil;

import java.util.Set;

/**
 * Base class for generated domain objects.
 */
public abstract class GeneratedDomainObject
    extends DomainObjectBase
{
    private final static JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

    public void setDomainType(String type)
    {
        throw new UnsupportedOperationException();
    }

    @JSONProperty(value = "_type", readOnly = true, priority = 100)
    public String getDomainType()
    {
        return this.getClass().getSimpleName();
    }


    @Override
    public Set<String> propertyNames()
    {
        return util.getAllPropertyNames(this);
    }


    @Override
    public Object getProperty(String name)
    {
        return util.getProperty(this, name);
    }


    @Override
    public void setProperty(String name, Object value)
    {
        util.setProperty(this, name,  value);
    }
}
