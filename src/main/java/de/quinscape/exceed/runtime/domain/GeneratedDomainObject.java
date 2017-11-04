package de.quinscape.exceed.runtime.domain;

import com.google.common.collect.Maps;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.svenson.JSONProperty;
import org.svenson.util.JSONBeanUtil;

import java.util.Map;
import java.util.Set;

/**
 * Base class for specialized, maven/JOOQ generated, POJO domain objects.
 */
public abstract class GeneratedDomainObject
    extends DomainObjectBase
{
    private final static JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

    public void setDomainType(String type)
    {
        throw new UnsupportedOperationException();
    }

    @JSONProperty(value = DomainType.TYPE_PROPERTY, readOnly = true, priority = 100)
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


    @Override
    public Map<String, Object> asMap()
    {
        final Set<String> propertyNames = util.getAllPropertyNames(this);
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(propertyNames.size());

        for (String propertyName : propertyNames)
        {
            map.put(propertyName, getProperty(propertyName));
        }
        return map;
    }
}
