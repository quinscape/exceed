package de.quinscape.exceed.model;

import org.svenson.DynamicProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractDynamicTopLevelModel
    extends TopLevelModel
    implements DynamicProperties
{
    private Map<String, Object> storage;


    @Override
    public void setProperty(String s, Object o)
    {
        if (storage == null)
        {
            storage = new HashMap<>();
        }

        storage.put(s, o);

    }


    @Override
    public Object getProperty(String s)
    {
        if (storage == null)
        {
            return null;
        }
        return storage.get(s);
    }


    @Override
    public Set<String> propertyNames()
    {
        if (storage == null)
        {
            return Collections.emptySet();
        }

        return storage.keySet();
    }


    @Override
    public boolean hasProperty(String s)
    {
        if (storage == null)
            return false;
        return storage.keySet().contains(s);
    }


    @Override
    public Object removeProperty(String s)
    {
        if (storage == null)
        {
            return null;
        }
        return storage.remove(s);
    }
}
