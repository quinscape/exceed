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
import de.quinscape.exceed.expression.ExpressionParserTreeConstants;
import de.quinscape.exceed.expression.LogicalOperatorNode;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.Operation;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ExpressionOperations(environment = QueryTransformerEnvironment.class)
public class QueryTransformerOperations
{
    private final static Logger log = LoggerFactory.getLogger(QueryTransformerOperations.class);

    private static final String FILTER_MARKER = QueryTransformerOperations.class.getName() + ":filter";

    private ExpressionService expressionService;


    public void setExpressionService(ExpressionService expressionService)
    {
        this.expressionService = expressionService;
    }


    @Operation
    public Object var(ExpressionContext<QueryTransformerEnvironment> ctx, String name)
    {
        return ctx.getEnv().getVars().get(name);
    }

    @Operation
    public Object prop(ExpressionContext<QueryTransformerEnvironment> ctx, String name)
    {
        QueryTransformerEnvironment env = ctx.getEnv();
        AttributeValue attribute = env.getComponentModel().getAttribute(name);
        if (attribute == null)
        {
            return null;
        }
        ASTExpression expression = attribute.getAstExpression();
        if (expression != null)
        {
            return expression.jjtAccept(env, null);
        }
        else
        {
            return attribute.getValue();
        }
    }

    @Operation
    public List<String> childModelNames(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        QueryTransformerEnvironment env = ctx.getEnv();
        List<String> names = new ArrayList<>();
        for (ComponentModel kid : env.getComponentModel().children())
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
    public List<String> childAttributes(ExpressionContext<QueryTransformerEnvironment> ctx, String cls, String attr)
    {
        if (attr == null)
        {
            attr = "name";
        }
        List<String> values = new ArrayList<>();
        attributeValueForClass(values, ctx.getEnv().getComponentModel(), cls, attr);
        return values;

    }


    private void attributeValueForClass(List<String> values, ComponentModel componentModel, String cls, String attr)
    {
        if (componentModel.getComponentRegistration().getDescriptor().getClasses().contains(cls))
        {
            AttributeValue attribute = componentModel.getAttribute(attr);
            if (attribute != null)
            {
                values.add(attribute.getValue());
            }
        }

        for (ComponentModel kid : componentModel.children())
        {
            attributeValueForClass(values, kid, cls, attr);
        }
    }

    @Operation(context = QueryDomainType.class)
    public QueryDefinition query(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return new QueryDefinition(queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition join(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition crossJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition fullOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition leftOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition rightOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition naturalJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    @Operation(context = QueryDomainType.class)
    public JoinDefinition naturalLeftOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }

    @Operation(context = QueryDomainType.class)
    public JoinDefinition naturalRightOuterJoin(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        return joinInternal(ctx, queryDomainType);
    }


    private JoinDefinition joinInternal(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        ASTFunction node = ctx.getASTFunction();
        Object result = ExpressionUtil.visitOneChildOf(ctx.getEnv(), node, ASTFunction.class, ASTPropertyChain.class, ASTIdentifier.class);
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


    @Operation(context = JoinDefinition.class)
    public QueryDomainType on(ExpressionContext<QueryTransformerEnvironment> ctx, JoinDefinition joinDefinition)
    {
        SimpleNode n = ExpressionUtil.expectChildOf(ctx.getASTFunction(), ASTEquality.class, ASTRelational.class, ASTLogicalAnd
            .class, ASTLogicalOr.class, ASTExpression.class);

        joinDefinition.setCondition(n);
        return joinDefinition.getLeft();
    }


    @Operation(context = QueryDomainType.class)
    public QueryDomainType fields(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        List<String> fields = new ArrayList<>();

        ASTFunction node = ctx.getASTFunction();

        int numKids = node.jjtGetNumChildren();
        for (int i = 0; i < numKids; i++)
        {
            Node kid = node.jjtGetChild(i);

            Object result = kid.jjtAccept(ctx.getEnv(), null);
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

    @Operation(context = QueryDomainType.class)
    public QueryDomainType as(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDomainType queryDomainType)
    {
        ASTFunction node = ctx.getASTFunction();
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


    @Operation(context = QueryDefinition.class)
    public QueryDefinition limit(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDefinition queryDefinition)
    {
        QueryTransformerEnvironment env = ctx.getEnv();

        ASTFunction node = ctx.getASTFunction();

        int len = node.jjtGetNumChildren();

        Integer limit;
        Node n;
        if (len == 1 && (n = node.jjtGetChild(0)) instanceof ASTFunction)
        {
            limit = (Integer) n.jjtAccept(env, null);
        }
        else
        {
            limit = (Integer) ExpressionUtil.visitOneChildOf(env, ctx.getASTFunction(), ASTInteger.class);
        }

        if ( limit != null)
        {
            queryDefinition.setLimit(limit);
        }
        return queryDefinition;
    }


    @Operation(context = QueryDefinition.class)
    public QueryDefinition offset(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDefinition queryDefinition)
    {
        QueryTransformerEnvironment env = ctx.getEnv();
        ASTFunction node = ctx.getASTFunction();
        int len = node.jjtGetNumChildren();

        Integer offset;
        Node n;
        if (len == 1 && (n = node.jjtGetChild(0)) instanceof ASTFunction)
        {
            offset = (Integer) n.jjtAccept(env, null);
        }
        else
        {
            offset = (Integer) ExpressionUtil.visitOneChildOf(env, node, ASTInteger.class);
        }

        if ( offset != null)
        {
            queryDefinition.setOffset(offset);
        }
        return queryDefinition;
    }


    @Operation(context = QueryDefinition.class)
    public QueryDefinition orderBy(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDefinition queryDefinition)
    {
        QueryTransformerEnvironment env = ctx.getEnv();
        ASTFunction node = ctx.getASTFunction();
        int len = node.jjtGetNumChildren();

        List<String> fields;

        Node n;
        if (len == 1 && (n = node.jjtGetChild(0)) instanceof ASTFunction)
        {
            fields = (List<String>) n.jjtAccept(env, null);
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


    @Operation(context = QueryDefinition.class)
    public QueryDefinition filter(ExpressionContext<QueryTransformerEnvironment> ctx, QueryDefinition queryDefinition)
    {
        QueryTransformerEnvironment env = ctx.getEnv();
        ASTFunction node = ctx.getASTFunction();

        SimpleNode n = ExpressionUtil.expectChildOf(ctx.getASTFunction(), ASTFunction.class, LogicalOperatorNode.class, ComparatorNode
            .class);

        ExpressionRenderer.render(n);

        if (n instanceof ASTFunction)
        {
            Object result = n.jjtAccept(env, null);
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

    @Operation
    public QueryDefinition query(ExpressionContext<QueryTransformerEnvironment> ctx)
    {
        Object result = ExpressionUtil.visitOneChildOf(ctx.getEnv(), ctx.getASTFunction(), ASTFunction.class, ASTPropertyChain.class);

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
}
