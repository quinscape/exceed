package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses Query expressions into {@link QueryDefinition}
 */
public class ComponentQueryTransformer
    implements QueryTransformer
{
    private final static Logger log = LoggerFactory.getLogger(ComponentQueryTransformer.class);

    private ExpressionService expressionService;


    public ComponentQueryTransformer(
        ExpressionService expressionService
    )
    {
        this.expressionService = expressionService;
    }




    @Override
    public Object transform(RuntimeContext runtimeContext, QueryContext queryContext, ASTExpression astExpression)
    {
        QueryTransformerEnvironment visitor = new QueryTransformerEnvironment(runtimeContext, queryContext);

        QueryDefinition result;

        final Object o = expressionService.evaluate(astExpression, visitor);

        if (o instanceof QueryDomainType)
        {
            return new QueryDefinition((QueryDomainType) o);
        }
        else
        {
            return o;
        }
    }
}
