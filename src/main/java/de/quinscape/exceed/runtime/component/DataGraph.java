package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.svenson.JSONProperty;
import org.svenson.util.JSONBeanUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataGraph
{
    public static final String WILDCARD_SYMBOL = "*";

    private final String qualifier;

    private Map<String, DomainProperty> columns;

    private DataGraphType type;

    private Object rootObject;

    private int count;

    private Object id;

    public DataGraph(Map<String, DomainProperty> columns, Object rootObject, int count)
    {
        this(columns, rootObject, count, null);
    }

    public DataGraph(Map<String, DomainProperty> columns, Object rootObject, int count, String qualifier)
    {
        if (columns == null)
        {
            throw new IllegalArgumentException("columns can't be null");
        }

        this.columns = columns;
        this.rootObject = rootObject;
        this.qualifier = qualifier;

        if (rootObject instanceof Collection)
        {
            type = DataGraphType.ARRAY;
            this.count = count;
        }
        else
        {
            type = DataGraphType.OBJECT;
            this.count = JSONBeanUtil.defaultUtil().getAllPropertyNames(rootObject).size();
        }
    }



    public Map<String, DomainProperty> getColumns()
    {
        return columns;
    }


    public void setColumns(Map<String, DomainProperty> columns)
    {
        this.columns = columns;
    }


    public Object getRootObject()
    {
        return rootObject;
    }

    @JSONProperty(ignore = true)
    public Collection<?> getRootCollection() {

        if (type != DataGraphType.ARRAY)
        {
            throw new IllegalStateException("Cannot get root collection from datagraph with type" + type);
        }
        
        return (Collection<?>) rootObject;
    }


    public void setRootObject(Object rootObject)
    {
        this.rootObject = rootObject;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + ", columns = " + columns
            + ", rootObject = " + rootObject
            ;
    }


    public void setCount(int count)
    {
        this.count = count;
    }


    public int getCount()
    {
        return count;
    }


    @JSONProperty(ignoreIfNull = true)
    public Object getId()
    {
        return id;
    }


    public void setId(Object id)
    {
        this.id = id;
    }


    /**
     * Returns a datalist with deep copied rootObject and shallow copied type information-
     *
     * @return
     */
    public DataGraph copy(RuntimeContext runtimeContext)
    {
        if (rootObject instanceof Collection)
        {
            Collection c = (Collection) rootObject;
            List copiedRows = new ArrayList<>(c.size());
            for (Object o : c)
            {
                Object copy = copyObject(runtimeContext, o);
                copiedRows.add(copy);
            }
            return new DataGraph(columns, copiedRows, count, qualifier);
        }
        else if (rootObject instanceof Map)
        {
            Map<String,Object> m = (Map<String,Object>) rootObject;
            Map<String,Object> copied = new HashMap<>(m.size());
            for (Map.Entry<String,Object> e : m.entrySet())
            {
                Object o = e.getValue();
                Object copy = copyObject(runtimeContext, o);
                copied.put(e.getKey(), copy);
            }
            return new DataGraph(columns, copied, count, qualifier);
        }
        else
        {
            throw new IllegalStateException("Unhandled root object" + rootObject);
        }
    }


    public Object copyObject(RuntimeContext runtimeContext, Object o)
    {
        Object copy;
        if (o instanceof DomainObject)
        {
            copy = DomainUtil.copy(runtimeContext, (DomainObject) o);
        }
        else if (o instanceof Map)
        {
            copy = new HashMap<>((Map)o);
        }
        else
        {
            throw new IllegalStateException("Don't know how to copy " + o);
        }
        return copy;
    }


    public void setType(DataGraphType type)
    {
        this.type = type;
    }


    public DataGraphType getType()
    {
        return type;
    }


    public String getQualifier()
    {
        return qualifier;
    }


    public boolean isMap()
    {
        return type == DataGraphType.OBJECT && columns.size() ==1 && columns.get(WILDCARD_SYMBOL) != null;
    }
}
