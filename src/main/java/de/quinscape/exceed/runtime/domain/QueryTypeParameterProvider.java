package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;

/**
 * Provides prepared statement parameters for query type queries.
 */
public interface QueryTypeParameterProvider
{
    /**
     * Provides the sql parameters for a query type model based on the given runtime context and query definition (which will be
     * a non-joined definition with a query domain type.
     *
     * @param runtimeContext        runtime context
     * @param queryDefinition       query definition
     *
     * @return  parameter value array
     */
    Object[] getSqlParameters(
        RuntimeContext runtimeContext,
        QueryDefinition queryDefinition
    );
}
