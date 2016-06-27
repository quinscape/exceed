package de.quinscape.exceed.model.context;

import de.quinscape.exceed.runtime.scope.ProcessContext;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.Map;

public class ContextModel
{
    private Map<String, ScopedListModel> lists;

    private Map<String, ScopedObjectModel> objects;

    private Map<String, ScopedPropertyModel> properties;


    public Map<String, ScopedListModel> getLists()
    {
        if (lists == null)
        {
            return Collections.emptyMap();
        }
        return lists;
    }


    @JSONTypeHint(ScopedListModel.class)
    public void setLists(Map<String, ScopedListModel> lists)
    {
        init(lists);
        this.lists = lists;
    }


    private static void init(Map<String, ? extends ScopedElementModel> elems)
    {
        for (Map.Entry<String, ? extends ScopedElementModel> entry : elems.entrySet())
        {
            String name = entry.getKey();
            if (ProcessContext.RESERVED_NAMES.contains(name))
            {
                throw new IllegalStateException("'" + name + "' is a reserved name.");
            }
            entry.getValue().setName(name);
        }
    }


    public Map<String, ScopedObjectModel> getObjects()
    {
        if (objects == null)
        {
            return Collections.emptyMap();
        }

        return objects;
    }


    @JSONTypeHint(ScopedObjectModel.class)
    public void setObjects(Map<String, ScopedObjectModel> objects)
    {
        init(objects);
        this.objects = objects;
    }


    public Map<String, ScopedPropertyModel> getProperties()
    {
        if (properties == null)
        {
            return Collections.emptyMap();
        }
        return properties;
    }


    @JSONTypeHint(ScopedPropertyModel.class)
    public void setProperties(Map<String, ScopedPropertyModel> properties)
    {
        init(properties);
        this.properties = properties;
    }
}
