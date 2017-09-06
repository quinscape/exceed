package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import org.springframework.jdbc.object.SqlQuery;

import javax.sql.DataSource;

public interface SqlQueryFactory
{
    SqlQuery<DomainObject> create(RuntimeContext runtimeContext, DataSource dataSource, QueryDefinition queryDefinition);
}
