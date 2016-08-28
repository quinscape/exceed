package de.quinscape.exceed.model.context;

import de.quinscape.exceed.runtime.scope.ProcessContext;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.Map;

public class ContextModel
{
    private Map<String, ScopedPropertyModel> properties;

    private static void init(Map<String, ScopedPropertyModel> elems)
    {
        for (Map.Entry<String, ScopedPropertyModel> entry : elems.entrySet())
        {
            String name = entry.getKey();
            if (ProcessContext.RESERVED_NAMES.contains(name))
            {
                throw new IllegalStateException("'" + name + "' is a reserved name.");
            }
            entry.getValue().setName(name);
        }
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
