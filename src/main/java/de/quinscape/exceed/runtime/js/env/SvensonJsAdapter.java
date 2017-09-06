package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.util.JSONUtil;
import jdk.nashorn.api.scripting.AbstractJSObject;

import java.util.Set;

/**
 * Adapter for Java Beans to have the Nashorn js engine access its properties with model / svenson JSON semantics / property names
 */
public class SvensonJsAdapter
    extends AbstractJSObject
{
    private final Object bean;


    public SvensonJsAdapter(Object bean)
    {
        this.bean = bean;
    }


    @Override
    public Object getMember(String name)
    {
        final Object propValue = JSONUtil.DEFAULT_UTIL.getProperty(bean, name);
        return wrap(propValue);
    }


    @Override
    public boolean hasMember(String name)
    {
        return keySet().contains(name);
    }


    @Override
    public void removeMember(String name)
    {
        JSONUtil.DEFAULT_UTIL.setProperty(bean, name, null);
    }


    @Override
    public void setMember(String name, Object value)
    {
        JSONUtil.DEFAULT_UTIL.setProperty(bean, name, unwrap(value));
    }


    @Override
    public Set<String> keySet()
    {
        return JSONUtil.DEFAULT_UTIL.getAllPropertyNames(bean);
    }

    public static Object wrap(Object value)
    {
        if (value instanceof DomainObject)
        {
            return new SvensonJsAdapter(value);
        }
        return value;
    }

    public static Object unwrap(Object value)
    {
        while (value instanceof SvensonJsAdapter)
        {
            value = ((SvensonJsAdapter) value).getBean();
        }
        return value;
    }

    public Object getBean()
    {
        return bean;
    }
}
