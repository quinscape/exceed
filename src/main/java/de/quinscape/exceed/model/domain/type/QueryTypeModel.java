package de.quinscape.exceed.model.domain.type;

import com.google.common.collect.Maps;
import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.Internal;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.QueryTypeUpdateHandler;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A domain type based on an SQL query.
 */
public class QueryTypeModel
    extends AbstractTopLevelModel
    implements DomainType
{
    private static final String DEFAULT_SQL_QUERY_FACTORY = "defaultQueryTypeSQLFactory";

    public static final String SQL_PARAM_INDEX = "sqlParamIndex";

    private static final String DEFAULT_QUERY_PARAMETER_PROVIDER = "defaultQueryParameterProvider";

    private static final String DEFAULT_DATA_SOURCE = "queryTypeDataSource";

    private String query;

    private List<DomainProperty> parameterTypes;
    private List<DomainProperty> columnTypes;

    private DomainService domainService;

    private String description;

    private String updateHandlerName;

    private String sqlQueryFactory = DEFAULT_SQL_QUERY_FACTORY;

    private String queryParameterProvider = DEFAULT_QUERY_PARAMETER_PROVIDER;

    private String countQuery;

    private ExpressionValue count;

    private Map<String, DomainProperty> parameterTypeMap;

    private Object data;

    private String dataSource = DEFAULT_DATA_SOURCE;

    /**
     * Returns the SQL query for this query type model.
     *
     * @return
     */
    public String getQuery()
    {
        return query;
    }


    public void setQuery(String query)
    {
        this.query = query;
    }

    @Override
    public <I, O> O accept(TopLevelModelVisitor<I, O> visitor, I in)
    {
        return visitor.visit(this);
    }


    public List<DomainProperty> getParameterTypes()
    {
        return parameterTypes;
    }

    @JSONProperty(ignore = true)
    public Map<String, DomainProperty> getParameterMap()
    {
        if (parameterTypeMap == null)
        {
            return Collections.emptyMap();
        }

        return parameterTypeMap;
    }

    @JSONTypeHint(DomainProperty.class)
    public void setParameterTypes(List<DomainProperty> parameterTypes)
    {
        this.parameterTypes = parameterTypes;

        final int numberOfTypes = parameterTypes.size();
        parameterTypeMap = Maps.newHashMapWithExpectedSize(numberOfTypes);
        for (int i = 0; i < numberOfTypes; i++)
        {
            DomainProperty parameterType = parameterTypes.get(i);

            setIndex(parameterType, i);
            parameterTypeMap.put(parameterType.getName(), parameterType);
        }
    }


    private void setIndex(DomainProperty parameterType, int i)
    {
        Map<String,Object> config = new HashMap<>();
        final Map<String, Object> configFromProperty = parameterType.getConfig();
        if (configFromProperty != null)
        {
            config.putAll(configFromProperty);
        }

        config.put(SQL_PARAM_INDEX, i);
        parameterType.setConfig(config);
    }


    @JSONTypeHint(DomainProperty.class)
    public void setColumnTypes(List<DomainProperty> columnTypes)
    {
        this.columnTypes = columnTypes;
    }


    /**
     * Property type definitions for the columns of the SQL query.
     */
    public List<DomainProperty> getColumnTypes()
    {
        return columnTypes;
    }

    @Override
    @Internal
    public List<DomainProperty> getProperties()
    {
        return getColumnTypes();
    }


    @Override
    public DomainProperty getProperty(String name)
    {
        return parameterTypeMap.get(name);
    }


    @Override
    public List<String> getPkFields()
    {
        return Collections.emptyList();
    }


    @Override
    @JSONProperty(ignore = true)
    public DomainService getDomainService()
    {
        return domainService;
    }


    @Override
    public boolean isPKField(String name)
    {
        return false;
    }


    @Override
    @Internal
    public boolean isSystem()
    {
        return false;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    /**
     * Description for this query type.
     */
    @Override
    public String getDescription()
    {
        return description;
    }


    @JSONProperty("dataSource")
    public void setDataSourceName(String dataSource)
    {
        this.dataSource = dataSource;
    }


    @Override
    public String getDataSourceName()
    {
        return dataSource;
    }


    @Override
    public void postProcess(ApplicationModel applicationModel)
    {
        for (DomainProperty columnType : columnTypes)
        {
            if (columnType.getDefaultValue() != null)
            {
                throw new InconsistentModelException("Query type column types can't have default values.");
            }
        }
    }


    public void setDomainService(DomainService domainService)
    {
        this.domainService = domainService;
    }


    /**
     * Spring bean name of an {@link QueryTypeUpdateHandler} implementation to use to update rows of this query type.
     * Default if <code>null</code>, disabling updating.
     */
    public String getUpdateHandlerName()
    {
        return updateHandlerName;
    }


    public void setUpdateHandlerName(String updateHandlerName)
    {
        this.updateHandlerName = updateHandlerName;
    }


    /**
     * Returns a class name of a {@link org.springframework.jdbc.object.SqlQuery} implementation used for
     * querying the results of this query type model.
     * 
     * @return sql query class name
     */
    public String getSqlQueryFactory()
    {
        return sqlQueryFactory;
    }


    public void setSqlQueryFactory(String sqlQueryFactory)
    {
        if (sqlQueryFactory == null)
        {
            throw new IllegalArgumentException("sqlQueryFactory can't be null");
        }

        this.sqlQueryFactory = sqlQueryFactory;
    }


    public static QueryTypeModel from(QueryDefinition queryDefinition)
    {
        final QueryDomainType queryDomainType = queryDefinition.getQueryDomainType();
        if (queryDomainType.getJoinedType() != null)
        {
            throw new IllegalStateException("Cannot query joined query domain types");
        }
        final DomainType type = queryDomainType.getType();
        if (!(type instanceof QueryTypeModel))
        {
            throw new IllegalStateException("Domain type " + type + " is not a query type");
        }

        return (QueryTypeModel) type;
    }


    /**
     * Spring bean name of a {@link de.quinscape.exceed.runtime.domain.QueryTypeParameterProvider} implementation.
     * Default is <code>"defaultQueryParameterProvider"</code>
     */
    public String getQueryParameterProvider()
    {
        return queryParameterProvider;
    }


    public void setQueryParameterProvider(String queryParameterProvider)
    {
        if (queryParameterProvider == null)
        {
            throw new IllegalArgumentException("queryParameterProvider can't be null");
        }

        this.queryParameterProvider = queryParameterProvider;
    }


    /**
     * Query to provide the maximum unpaginated row count available to the main query.
     */
    public String getCountQuery()
    {
        return countQuery;
    }


    public void setCountQuery(String countQuery)
    {
        this.countQuery = countQuery;
    }


    /**
     * Count expression to evaluate instead of executing a separate count query.
     */
    public String getCount()
    {
        return count.getValue();
    }


    public void setCount(String count)
    {
        this.count = ExpressionValue.forValue(count, true);
    }

    @JSONProperty(ignore = true)
    public ExpressionValue getCountValue()
    {
        return count;
    }

    /**
     * Returns arbitrary user data associated with this query type model. Can be used by alternate storage configuration
     * for a query type.
     *
     * @return  data
     */
    public Object getData()
    {
        return data;
    }


    public void setData(Object data)
    {
        this.data = data;
    }


    /**
     * Name of the query type. Should start With a "Q" followed by an uppercase letter.
     * @return
     */
    @Override
    public String getName()
    {
        return super.getName();
    }
}
