package de.quinscape.exceed.runtime.domain;

import org.svenson.JSONProperty;
import org.svenson.util.JSONBeanUtil;

import java.util.Set;

/**
 * Base class for generated domain objects.
 */
public abstract class GeneratedDomainObject
    extends AbstractDomainObject
{
    private final static JSONBeanUtil util = JSONBeanUtil.defaultUtil();

    public void setType(String type)
    {
        throw new UnsupportedOperationException();
    }

    @JSONProperty(value = "_type", readOnly = true, priority = 100)
    public String getType()
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
