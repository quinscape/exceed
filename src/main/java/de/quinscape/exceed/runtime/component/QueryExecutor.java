package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;

import java.util.Map;

/**
 * Implemented by classes executing {@link QueryDefinition}s.
 */
public interface QueryExecutor
{
    /**
     * Executes the give query definition returning result object
     * to sent as JSON to the client.
     *
     *
     * @param runtimeContext
     * @param queryDefinition      query definition
     *
     * @return query result
     */
    Object execute(RuntimeContext runtimeContext, QueryDefinition queryDefinition);
}
