package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.runtime.RuntimeContext;

/**
 *
 */
public interface QueryTransformer
{
    /**
     * Transforms the given prepared query object to the final query definition object that can be executed by the
     * query executor associated with the query transformer.
     *
     * @param runtimeContext    runtime context
     * @param queryContext      query context
     * @param astExpression     query expression to transform
     *
     * @return  query definition
     */
    Object transform(RuntimeContext runtimeContext, QueryContext queryContext, ASTExpression astExpression);
}
