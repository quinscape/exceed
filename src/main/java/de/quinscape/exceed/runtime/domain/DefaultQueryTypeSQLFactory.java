package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import org.springframework.jdbc.object.SqlQuery;

import javax.sql.DataSource;

public class DefaultQueryTypeSQLFactory
    implements SqlQueryFactory
{
    @Override
    public SqlQuery<DomainObject> create(
        RuntimeContext runtimeContext,
        DataSource dataSource,
        QueryDefinition queryDefinition
    )
    {
        QueryTypeModel queryTypeModel = QueryTypeModel.from(queryDefinition);


        final DefaultQueryTypeQuery query = new DefaultQueryTypeQuery(
            runtimeContext,
            dataSource,
            queryTypeModel
        );

        return query;
    }
}
