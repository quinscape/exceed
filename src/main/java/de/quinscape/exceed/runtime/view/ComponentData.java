package de.quinscape.exceed.runtime.view;

import de.quinscape.exceed.runtime.component.QueryError;
import org.svenson.JSONProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ComponentData
{
    private final Object vars;

    private final Object data;

    private final List<QueryError> errors;


    public ComponentData( Object vars, Map<String, Object> componentDataMap)
    {
        List<QueryError> errors = new ArrayList<>();

        final Iterator<Object> iterator = componentDataMap.values().iterator();
        while (iterator.hasNext())
        {
            final Object value = iterator.next();

            if (value instanceof QueryError)
            {
                errors.add((QueryError) value);
                iterator.remove();
            }
        }

        this.vars = vars;
        this.data = componentDataMap;
        this.errors = errors;
    }

    public ComponentData(Object vars, Object data)
    {
        this.vars = vars;
        this.data = data;
        this.errors = null;
    }

    public Object getData()
    {
        return data;
    }

    public Object getVars()
    {
        return vars;
    }


    @JSONProperty(ignore = true)
    public List<QueryError> getErrors()
    {
        return errors;
    }
}
