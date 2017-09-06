package de.quinscape.exceed.runtime.component;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.util.DomainUtil;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataGraph
{
    public static final String WILDCARD_SYMBOL = "*";

    public static final String QUERY_QUALIFIER = "Query";

    private String qualifier;

    private Map<String, DomainProperty> columns;

    private DataGraphType type;

    private Object rootObject;

    private int count;

    public final static Map<String,DomainProperty> DEFAULT_COLUMNS = ImmutableMap.of(WILDCARD_SYMBOL, ExpressionUtil.OBJECT_TYPE);


    public DataGraph()
    {
        this(DEFAULT_COLUMNS, Collections.emptyMap(), 1, null);
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
            this.count = 1;
        }
    }



    public Map<String, DomainProperty> getColumns()
    {
        return columns;
    }


    @JSONTypeHint(DomainProperty.class)
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


    public void setQualifier(String qualifier)
    {
        this.qualifier = qualifier;
    }


    public String getQualifier()
    {
        return qualifier;
    }


    public boolean isMap()
    {
        return type == DataGraphType.OBJECT && columns.size() ==1 && columns.get(WILDCARD_SYMBOL) != null;
    }


    public DomainObject extract(RuntimeContext runtimeContext, String typeOrName)
    {
        final DomainObject domainObject = extractByType(runtimeContext, typeOrName);
        if (domainObject != null)
        {
            return domainObject;
        }
        return extractByQueryName(runtimeContext, typeOrName);
    }

    public DomainObject extractByType(RuntimeContext runtimeContext, String domainType)
    {
        ensureSlice();

        DomainObject fromRoot = resolveFromRootByType(runtimeContext, domainType, 1).get(0);
        if (fromRoot != null)
        {
            return null;
        }

        return resolveEmbeddedByType(runtimeContext, domainType);
    }


    public DomainObject extractByQueryName(RuntimeContext runtimeContext, String queryName)
    {
        ensureSlice();

        List<DomainProperty> properties = new ArrayList<>(columns.size());
        
        for (DomainProperty domainProperty : columns.values())
        {
            final String currentQueryType = getQueryName(domainProperty);
            if (currentQueryType != null && currentQueryType.equals(queryName))
            {
                properties.add(domainProperty);
            }
        }

        if (properties.size() == 0)
        {
            throw new IllegalArgumentException("Unknown query name '" + queryName + "' in " + this);
        }

        final Map<String,Object> map = (Map<String, Object>) getRootCollection().iterator().next();


        // if our query type is an embedded type, we should have a single column with an embedded domain object
        if (properties.size() == 1 && properties.get(0).getType().equals(PropertyType.DOMAIN_TYPE))
        {
            // which we use
            return (DomainObject) map.get(properties.get(0).getName());
        }


        // otherwise our domain object query type was spread over the root columns and we collect the properties
        DomainObject domainObject = runtimeContext.getDomainService().create(runtimeContext, properties.get(0).getDomainType(), null);

        for (DomainProperty domainProperty : columns.values())
        {
            if (queryName.equals(getQueryName(domainProperty)))
            {
                final String propertyName = domainProperty.getName();
                domainObject.setProperty(propertyName, map.get(propertyName));
            }
        }
        return domainObject;
    }

    private List<DomainObject> resolveFromRootByType(
        RuntimeContext runtimeContext, String domainType, int limit
    )
    {
        if (runtimeContext == null)
        {
            throw new IllegalArgumentException("runtimeContext can't be null");
        }

        if (domainType == null)
        {
            throw new IllegalArgumentException("domainType can't be null");
        }

        String queryType = null;
        for (DomainProperty domainProperty : columns.values())
        {
            final String currentQueryType = getQueryName(domainProperty);
            
            if (currentQueryType != null)
            {
                final String currentType = domainProperty.getDomainType();
                
                if (currentType.equals(domainType))
                {
                    if (queryType != null && !currentQueryType.equals(queryType))
                    {
                        return null;
                    }
                    queryType = currentQueryType;
                }
            }
        }

        if (queryType == null)
        {
            return null;
        }


        List<DomainObject> list = new ArrayList<>();

        for (Object o : getRootCollection())
        {
            DomainObject domainObject = runtimeContext.getDomainService().create(runtimeContext, domainType, null);


            if (o instanceof Map)
            {
                final Map<String, Object> map = (Map<String, Object>) o;

                for (Map.Entry<String, DomainProperty> e : columns.entrySet())
                {
                    final String columnName = e.getKey();
                    final DomainProperty domainProperty = e.getValue();
                    if (queryType.equals(getQueryName(domainProperty)))
                    {
                        final String propertyName = domainProperty.getName();
                        domainObject.setProperty(propertyName, map.get(columnName));
                    }
                }
            }
            else if (o instanceof DomainObject)
            {
                final DomainObject src = (DomainObject) o;
                for (Map.Entry<String, DomainProperty> e : columns.entrySet())
                {
                    final String columnName = e.getKey();
                    final DomainProperty domainProperty = e.getValue();

                    if (queryType.equals(getQueryName(domainProperty)))
                    {
                        final String propertyName = domainProperty.getName();
                        domainObject.setProperty(propertyName, src.getProperty(columnName));
                    }
                }
            }
            else
            {
                throw new IllegalStateException("Invalid row object: " + o);
            }

            list.add(domainObject);

            if (limit > 0 && list.size() == limit)
            {
                break;
            }
        }

        return list;
    }

    private DomainObject resolveEmbeddedByType(
        RuntimeContext runtimeContext, String domainType
    )
    {
        String queryType = null;
        String columnName = null;
        for (DomainProperty domainProperty : columns.values())
        {
            final String currentQueryType = getQueryName(domainProperty);
            if (currentQueryType != null && isDomainObject(domainProperty, domainType))
            {
                if (queryType != null && !currentQueryType.equals(queryType))
                {
                    return null;
                }
                queryType = currentQueryType;
                columnName = domainProperty.getName();
            }
        }

        if (queryType == null)
        {
            return null;
        }

        DomainObject domainObject = runtimeContext.getDomainService().create(runtimeContext, domainType, null);

        return (DomainObject) ((Map<String, Object>) getRootCollection().iterator().next()).get(columnName);
    }


    private boolean isDomainObject(DomainProperty domainProperty, String domainType)
    {
        return domainProperty.getType().equals(PropertyType.DOMAIN_TYPE) && domainProperty.getTypeParam().equals(
            domainType);
    }


    public String getQueryName(DomainProperty domainProperty)
    {

        final Map<String, Object> config = domainProperty.getConfig();
        if (config == null)
        {
            return null;
        }
        return (String) config.get(QueryDefinition.QUERY_NAME_CONFIG);
    }


    private void ensureSlice()
    {
        if (isMap())
        {
            throw new IllegalStateException("Cannot extract from map data graph: "+ this);
        }

        if (getRootCollection().size() > 1)
        {
            throw new IllegalStateException("Cannot extract single domain object from list data graph with more than 1 row");
        }
    }


    private String determineUnambiguousDomainType()
    {
        String domainType = null;
        DomainProperty prev = null;
        for (DomainProperty domainProperty : getColumns().values())
        {
            final String current = domainProperty.getDomainType();

            if (current != null)
            {
                if (domainType == null)
                {
                    domainType = current;
                    prev = domainProperty;
                }
                else if (!domainType.equals(current))
                {
                    throw new IllegalStateException("Graph contains more than one domain type: conflicting properties are " + prev + " and " + domainType);
                }
            }
        }

        return domainType;
    }


    public List<DomainObject> extractList(RuntimeContext runtimeContext, String domainType)
    {
        return resolveFromRootByType(runtimeContext, domainType, -1);
    }
}
