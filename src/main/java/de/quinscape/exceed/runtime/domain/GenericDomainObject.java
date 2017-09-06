package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.type.DomainType;
import org.svenson.DynamicProperties;
import org.svenson.JSONProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * General purpose, unspecialised container for domain object data.
 *
 * @see GeneratedDomainObject
 */
public class GenericDomainObject
    extends DomainObjectBase
    implements DomainObject, DynamicProperties
{

    private Map<String,Object> content;

    public GenericDomainObject()
    {
        content = new HashMap<>();
    }

    @Override
    @JSONProperty(ignore = true)
    public String getId()
    {
        return (String) getProperty(DomainType.ID_PROPERTY);
    }


    @Override
    public void setId(String id)
    {
        setProperty(DomainType.ID_PROPERTY, id);
    }

    public void setDomainType(String type)
    {
        setProperty(DomainType.TYPE_PROPERTY, type);
    }


    @Override
    public Set<String> propertyNames()
    {
        return content.keySet();
    }


    @Override
    public boolean hasProperty(String name)
    {
        return content.containsKey(name);
    }


    @Override
    public Object removeProperty(String name)
    {
        return content.remove(name);
    }


    @Override
    public Object getProperty(String name)
    {
        return content.get(name);
    }


    @Override
    public void setProperty(String name, Object value)
    {
        content.put(name, value);
    }


    @Override
    @JSONProperty(value = DomainType.TYPE_PROPERTY, priority = 100)
    public String getDomainType()
    {
        return (String) getProperty(DomainType.TYPE_PROPERTY);
    }


    public static final String SEP = System.getProperty("line.separator");

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (String name : propertyNames())
        {
            Object value = getProperty(name);
            sb.append("  ").append(name).append(" = ").append(value).append(value != null ? " ( " + value.getClass() + " )" : "" ).append(SEP);
        }

        return getClass().getName() + "@" + Integer.toHexString(hashCode()) + ":\n" + sb;
    }
}
