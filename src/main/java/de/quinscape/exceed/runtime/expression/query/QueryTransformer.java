package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Parses Query expressions into {@link QueryDefinition}
 */
public class QueryTransformer
{
    private final static Logger log = LoggerFactory.getLogger(QueryTransformer.class);

    private ExpressionService expressionService;

    private final StorageConfigurationRepository storageConfigurationRepository;


    public QueryTransformer(ExpressionService expressionService, StorageConfigurationRepository storageConfigurationRepository)
    {
        this.expressionService = expressionService;
        this.storageConfigurationRepository = storageConfigurationRepository;
    }


    public QueryDefinition transform(RuntimeContext runtimeContext, String expression, ComponentModel
        componentModel, Map<String, Object> vars) throws ParseException
    {
        ASTExpression astExpression = ExpressionParser.parse(expression);

        Object o = evaluate(runtimeContext, astExpression,
            componentModel, vars);

        if (o instanceof QueryDefinition)
        {
            return (QueryDefinition) o;
        }
        if (o instanceof QueryDomainType)
        {
            return new QueryDefinition((QueryDomainType) o);
        }
        throw new QueryTransformationException("Expression evaluated to invalid result: " + o);
    }


    /**
     * Evaluates the given ASTExpression in the query transformer environment.
     *
     * @param runtimeContext    runtimeContext
     * @param astExpression     ast expression
     * @param componentModel    component model
     * @param vars              vars for this component model
     * @return
     */
    public Object evaluate(RuntimeContext runtimeContext, ASTExpression astExpression, ComponentModel componentModel, Map<String, Object> vars)
    {
        QueryTransformerEnvironment visitor = new QueryTransformerEnvironment(runtimeContext, storageConfigurationRepository, componentModel, vars);
        return expressionService.evaluate(astExpression, visitor);
    }

}
