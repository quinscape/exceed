package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.object.SqlQuery;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Domain operations implementation for query types.
 *
 * @see QueryTypeModel
 */
public class QueryTypeOperations
    implements DomainOperations
{
    private final JdbcTemplate jdbcTemplate;


    private final Map<String, SqlQueryFactory> sqlQueryFactories;

    private final Map<String, QueryTypeParameterProvider> queryParameterProviders;

    private final Map<String, QueryTypeUpdateHandler> handlers;


    public QueryTypeOperations(
        JdbcTemplate jdbcTemplate,
        Map<String, SqlQueryFactory> sqlQueryFactories,
        Map<String, QueryTypeParameterProvider> queryParameterProviders,
        Map<String, QueryTypeUpdateHandler> handlers
    )
    {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueryFactories = sqlQueryFactories;
        this.queryParameterProviders = queryParameterProviders;
        this.handlers = handlers;
    }


    private <T> T get(Class<T> cls, Map<String,T> map, String name)
    {
        final T value = map.get(name);

        if (value == null)
        {
            throw new IllegalStateException("No " + cls.getSimpleName() + " found with name '" + name + "'" );
        }

        return value;
    }

    @Override
    public DataGraph query(RuntimeContext runtimeContext, DomainService domainService, QueryDefinition queryDefinition)
    {

        final QueryTypeModel queryTypeModel = QueryTypeModel.from(queryDefinition);

        SqlQueryFactory factory = get(SqlQueryFactory.class, sqlQueryFactories, queryTypeModel.getSqlQueryFactory());
        QueryTypeParameterProvider parameterProvider = get(QueryTypeParameterProvider.class, queryParameterProviders, queryTypeModel.getQueryParameterProvider());

        SqlQuery<DomainObject> sqlQuery = factory.create(runtimeContext, jdbcTemplate.getDataSource(), queryDefinition);

        final Object[] parameters = parameterProvider.getSqlParameters(runtimeContext, queryDefinition);

        final List<DomainObject> list = sqlQuery.execute(
            parameters,
            Collections.emptyMap()
        );

        final int count;

        final ExpressionValue countValue = queryTypeModel.getCountValue();
        if (countValue != null)
        {
            count = (int) runtimeContext.getJsEnvironment().getValue(runtimeContext, countValue.getAstExpression());
        }
        else
        {
            final String countQuery = queryTypeModel.getCountQuery();
            if (countQuery != null)
            {
                count = jdbcTemplate.queryForObject(
                    countQuery,
                    parameters,
                    Integer.class
                );
            }
            else
            {
                count = Integer.MAX_VALUE;
            }
        }

        return new DataGraph(queryDefinition.createColumnDescriptorMap(), list, count, null);
    }

    @Override
    public DomainObject create(RuntimeContext runtimeContext, DomainService domainService, String type, String id,
                               Class<? extends DomainObject> implClass)
    {
        return CommonDomainOperations.create(
            runtimeContext,
            domainService,
            type,
            id,
            implClass
        );
    }


    @Override
    public DomainObject read(RuntimeContext runtimeContext, DomainService domainService, String type, String id)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean delete(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
    {
        throw new UnsupportedOperationException();

    }


    @Override
    public void insert(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
    {
        updateInternal(runtimeContext, domainService, genericDomainObject, UpdateType.INSERT);
    }

    @Override
    public void insertOrUpdate(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
    {
        updateInternal(runtimeContext, domainService, genericDomainObject, UpdateType.INSERT_OR_UPDATE);
    }

    @Override
    public boolean update(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
    {
        updateInternal(runtimeContext, domainService, genericDomainObject, UpdateType.UPDATE);
        return false;
    }

    private void updateInternal(
        RuntimeContext runtimeContext,
        DomainService domainService,
        DomainObject genericDomainObject,
        UpdateType updateType)
    {

        final String name = genericDomainObject.getDomainType();
        final DomainType domainType = domainService.getDomainType(name);

        if (!(domainType instanceof QueryTypeModel))
        {
            throw new IllegalStateException("Domain type " + name + "is not a query type model");
        }

        QueryTypeModel queryTypeModel = (QueryTypeModel)domainType;
        final String updateHandlerName = queryTypeModel.getUpdateHandlerName();
        if (updateHandlerName == null)
        {
            throw new IllegalStateException("No update handler defined on query domain type '" + name + "'");
        }

        final QueryTypeUpdateHandler handler = handlers.get(updateHandlerName);
        if (handler == null)
        {
            throw new IllegalStateException("Update handler ' " + updateHandlerName + "' not found.");
        }

        handler.update(runtimeContext, genericDomainObject, updateType);
    }
}
