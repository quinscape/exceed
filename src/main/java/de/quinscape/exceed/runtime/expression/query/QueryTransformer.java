package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTLogicalAnd;
import de.quinscape.exceed.expression.ASTLogicalOr;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTRelational;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ComparatorNode;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ExpressionParserTreeConstants;
import de.quinscape.exceed.expression.LogicalOperatorNode;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.expression.Operation;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses Query expressions into {@link QueryDefinition}
 */
public class QueryTransformer
{
    private static Logger log = LoggerFactory.getLogger(QueryTransformer.class);

    private static final String FILTER_MARKER = QueryTransformer.class.getName() + ":filter";


    public QueryDefinition transform(DomainService domainService, String expression, ComponentModel
        componentModel, Map<String, Object> vars) throws ParseException
    {
        ASTExpression astExpression = ExpressionParser.parse(expression);

        Object o = evaluate(domainService, astExpression,
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
     * @param domainService     domain service
     * @param astExpression     ast expression
     * @param componentModel    component model
     * @param vars              vars for this component model
     * @return
     */
    public Object evaluate(DomainService domainService, ASTExpression astExpression, ComponentModel componentModel, Map<String, Object> vars)
    {
        QueryTransformerEnvironment visitor = new QueryTransformerEnvironment(domainService, componentModel, vars);
        return astExpression.jjtAccept(visitor, null);
    }


    public static class QueryTransformerEnvironment
        extends ExpressionEnvironment
    {

        private final ComponentModel componentModel;

        private final Map<String, Object> vars;

        private final DomainService domainService;


        private QueryTransformerEnvironment(DomainService domainService, ComponentModel
            componentModel, Map<String, Object> vars)
        {
            this.domainService = domainService;
            this.componentModel = componentModel;
            this.vars = vars;
        }


        @Operation
        public Object var(ASTFunction node)
        {
            String name = getArg(node, 0, String.class);
            return vars.get(name);
        }

        @Operation
        public Object prop(ASTFunction node)
        {
            String name = getArg(node, 0, String.class);
            AttributeValue attribute = componentModel.getAttribute(name);
            if (attribute == null)
            {
                return null;
            }
            ASTExpression expression = attribute.getAstExpression();
            if (expression != null)
            {
                return expression.jjtAccept(this, null);
            }
            else
            {
                return attribute.getValue();
            }
        }


        @Override
        protected Object resolveIdentifier(String name)
        {

            DomainType domainType = domainService.getDomainType(name);
            if (domainType == null)
            {
                throw new QueryTransformationException("Unknown domain type '" + name + "'");
            }
            QueryDomainType queryDomainType = new QueryDomainType(domainType);


            log.debug("Resolving {} to {}", name, queryDomainType);

            return queryDomainType;
        }

        @Operation
        public QueryDefinition query(ASTFunction node)
        {
            Object result = visitOneChildOf(node, ASTFunction.class, ASTPropertyChain.class);

            if (result == null)
            {
                return new QueryDefinition(null);
            }

            if (!(result instanceof QueryDomainType))
            {
                throw new QueryTransformationException("Argument to query() is no domain type definition: "
                    + result);

            }

            return new QueryDefinition((QueryDomainType) result);
        }


        @Operation
        public List<String> childModelNames(ASTFunction node)
        {
            List<String> names = new ArrayList<>();
            for (ComponentModel kid : componentModel.children())
            {
                AttributeValue name = kid.getAttribute("name");
                if (name != null)
                {
                    names.add((String) name.getValue());
                }
            }

            return names;
        }


        @Operation
        public QueryDefinition query(ASTFunction node, QueryDomainType queryDomainType)
        {
            return new QueryDefinition(queryDomainType);
        }


        @Operation
        public JoinDefinition join(ASTFunction node, QueryDomainType queryDomainType)
        {
            return joinInternal(node, queryDomainType);
        }


        @Operation
        public JoinDefinition crossJoin(ASTFunction node, QueryDomainType queryDomainType)
        {
            return joinInternal(node, queryDomainType);
        }


        @Operation
        public JoinDefinition fullOuterJoin(ASTFunction node, QueryDomainType queryDomainType)
        {
            return joinInternal(node, queryDomainType);
        }


        @Operation
        public JoinDefinition leftOuterJoin(ASTFunction node, QueryDomainType queryDomainType)
        {
            return joinInternal(node, queryDomainType);
        }


        @Operation
        public JoinDefinition rightOuterJoin(ASTFunction node, QueryDomainType queryDomainType)
        {
            return joinInternal(node, queryDomainType);
        }


        @Operation
        public JoinDefinition naturalJoin(ASTFunction node, QueryDomainType queryDomainType)
        {
            return joinInternal(node, queryDomainType);
        }


        @Operation
        public JoinDefinition naturalLeftOuterJoin(ASTFunction node, QueryDomainType queryDomainType)
        {
            return joinInternal(node, queryDomainType);
        }

        @Operation
        public JoinDefinition naturalRightOuterJoin(ASTFunction node, QueryDomainType queryDomainType)
        {
            return joinInternal(node, queryDomainType);
        }


        private JoinDefinition joinInternal(ASTFunction node, QueryDomainType queryDomainType)
        {
            Object result = visitOneChildOf(node, ASTFunction.class, ASTPropertyChain.class, ASTIdentifier.class);
            String joinName = node.getName();
            if (result instanceof QueryDomainType)
            {
                QueryDomainType other = (QueryDomainType) result;
                return queryDomainType.join(joinName, other);
            }
            else
            {
                throw new QueryTransformationException("Invalid argument for " + joinName + "(): " + result + " ( was:" + node + ")");
            }
        }


        @Operation
        public QueryDomainType on(ASTFunction node, JoinDefinition joinDefinition)
        {
            SimpleNode n = expectChildOf(node, ASTEquality.class, ASTRelational.class, ASTLogicalAnd
                .class, ASTLogicalOr.class, ASTExpression.class);

            joinDefinition.setCondition(n);
            return joinDefinition.getLeft();
        }


        @Operation
        public QueryDomainType fields(ASTFunction node, QueryDomainType queryDomainType)
        {
            List<String> fields = new ArrayList<>();

            int numKids = node.jjtGetNumChildren();
            for (int i = 0; i < numKids; i++)
            {
                Node kid = node.jjtGetChild(i);

                Object result = kid.jjtAccept(this, null);
                if (result instanceof List)
                {
                    fields.addAll((List)result);
                }
                else if (result instanceof String)
                {
                    fields.add((String) result);
                }
            }
            queryDomainType.setFields(fields);
            return queryDomainType;
        }

        @Operation
        public QueryDomainType as(ASTFunction node, QueryDomainType queryDomainType)
        {
            Node arg = node.jjtGetNumChildren() > 0 ? node.jjtGetChild(0) : null;
            String alias;
            if (arg instanceof ASTString)
            {
                alias = ((ASTString) arg).getValue();
            }
            else if (arg instanceof ASTIdentifier)
            {
                alias = ((ASTIdentifier) arg).getName();
            }
            else
            {
                throw new QueryTransformationException("Invalid arg for as(): " + arg);
            }
            queryDomainType.setAlias(alias);
            return queryDomainType;
        }


        @Operation
        public QueryDefinition limit(ASTFunction node, QueryDefinition queryDefinition)
        {
            int len = node.jjtGetNumChildren();

            Integer limit;
            Node n;
            if (len == 1 && (n = node.jjtGetChild(0)) instanceof ASTFunction)
            {
                limit = (Integer) n.jjtAccept(this, null);
            }
            else
            {
                limit = (Integer) visitOneChildOf(node, ASTInteger.class);
            }

            if ( limit != null)
            {
                queryDefinition.setLimit(limit);
            }
            return queryDefinition;
        }


        @Operation
        public QueryDefinition offset(ASTFunction node, QueryDefinition queryDefinition)
        {
            int len = node.jjtGetNumChildren();

            Integer offset;
            Node n;
            if (len == 1 && (n = node.jjtGetChild(0)) instanceof ASTFunction)
            {
                offset = (Integer) n.jjtAccept(this, null);
            }
            else
            {
                offset = (Integer) visitOneChildOf(node, ASTInteger.class);
            }

            if ( offset != null)
            {
                queryDefinition.setOffset(offset);
            }
            return queryDefinition;
        }


        @Operation
        public QueryDefinition orderBy(ASTFunction node, QueryDefinition queryDefinition)
        {
            int len = node.jjtGetNumChildren();

            List<String> fields;

            Node n;
            if (len == 1 && (n = node.jjtGetChild(0)) instanceof ASTFunction)
            {
                fields = (List<String>) n.jjtAccept(this, null);
            }
            else
            {
                fields = new ArrayList<>(len);


                for (int i = 0; i < len; i++)
                {

                    n = node.jjtGetChild(i);
                    fields.add(evaluateOrderByArg(n));
                }
            }

            if (fields != null)
            {
                queryDefinition.setOrderBy(fields);
            }

            return queryDefinition;
        }


        @Operation
        public QueryDefinition filter(ASTFunction node, QueryDefinition queryDefinition)
        {
            SimpleNode n = expectChildOf(node, ASTFunction.class, LogicalOperatorNode.class, ComparatorNode
                .class);

            if (n instanceof ASTFunction)
            {
                Object result = n.jjtAccept(this, null);
                if (result == null)
                {
                    return queryDefinition;
                }
                if (!(result instanceof LogicalOperatorNode) && !(result instanceof ComparatorNode))
                {
                    throw new QueryTransformationException("Return value of " + n + " is no filter value:" + result);
                }

            }

            SimpleNode filter = queryDefinition.getFilter();
            if (filter != null)
            {
                // if we already have a filter, we AND the old filter
                // we don't want to change the filter instances here cause they might be directly
                // imported from a component model, so we mark nodes that we have created here with
                // a constant value

                // is the filter an marked AND?
                if (filter instanceof ASTLogicalAnd && filter.jjtGetValue() == FILTER_MARKER)
                {
                    // yes -> just add the new filter to the AND
                    filter.jjtAddChild(n, filter.jjtGetNumChildren());
                }
                else
                {   // no -> create a new marked AND combining both filters and set that as filter
                    ASTLogicalAnd newAnd = new ASTLogicalAnd(ExpressionParserTreeConstants.JJTLOGICALAND);
                    newAnd.jjtAddChild(n, 1);
                    newAnd.jjtAddChild(filter, 0);
                    newAnd.jjtSetValue(FILTER_MARKER);
                    queryDefinition.setFilter(newAnd);
                }
            }
            else
            {
                // if we have no filter, just set the new one
                queryDefinition.setFilter(n);
            }
            return queryDefinition;
        }

        private String evaluateOrderByArg(Node n)
        {
            if (n instanceof ASTIdentifier)
            {
                return ((ASTIdentifier) n).getName();
            }
            else if (n instanceof ASTPropertyChain)
            {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < n.jjtGetNumChildren(); i++)
                {
                    Node kid = n.jjtGetChild(i);

                    if (i > 0)
                    {
                        sb.append('.');
                    }

                    if (kid instanceof ASTIdentifier)
                    {
                        sb.append(((ASTIdentifier) kid).getName());
                    }
                    else
                    {
                        throw new QueryTransformationException("Invalid node in orderBy arguments: " + kid);
                    }
                }
                return sb.toString();
            }
            else
            {
                throw new QueryTransformationException("Invalid node in orderBy arguments: " + n);
            }
        }
    }
}
