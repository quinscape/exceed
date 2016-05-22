package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTLogicalAnd;
import de.quinscape.exceed.expression.ASTLogicalOr;
import de.quinscape.exceed.expression.ASTNegate;
import de.quinscape.exceed.expression.ASTNot;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTRelational;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryFilterEnvironment
    extends ExpressionEnvironment
{
    private static Logger log = LoggerFactory.getLogger(QueryFilterEnvironment.class);


    private final QueryTransformerEnvironment transformerEnvironment;

    private final QueryDomainType queryDomainType;

    private ComponentModel columnContext;


    public QueryFilterEnvironment(QueryTransformerEnvironment transformerEnvironment, QueryDomainType queryDomainType)
    {
        this.transformerEnvironment = transformerEnvironment;
        this.queryDomainType = queryDomainType;
    }


    @Override
    protected boolean logicalOperatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean comparatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean complexLiteralsAllowed()
    {
        return false;
    }


    @Override
    protected boolean arithmeticOperatorsAllowed()
    {
        return false;
    }


    @Override
    protected boolean expressionSequenceAllowed()
    {
        return false;
    }


    @Override
    public Object resolveIdentifier(String name)
    {
        return DSL.field(DSL.name(name));
    }


    @Override
    public Object visit(ASTLogicalOr node, Object data)
    {
        Condition condition = null;

        for (int i=0; i < node.jjtGetNumChildren(); i++)
        {
            Node n = node.jjtGetChild(i);
            Object value = n.jjtAccept(this, null);

            if (!(value instanceof Condition))
            {
                throw new IllegalArgumentException(n + " is no condition");
            }

            if (i == 0)
            {
                condition = (Condition) value;
            }
            else
            {
                condition = condition.or((Condition) value);

            }
        }

        return condition;
    }


    @Override
    public Object visit(ASTLogicalAnd node, Object data)
    {
        Condition condition = null;

        for (int i=0; i < node.jjtGetNumChildren(); i++)
        {
            Node n = node.jjtGetChild(i);
            if (n != null)
            {
                Object value = n.jjtAccept(this, null);
                if (value != null)
                {
                    if (!(value instanceof Condition))
                    {
                        throw new IllegalArgumentException(n + " is no condition");
                    }

                    if (condition == null)
                    {
                        condition = (Condition) value;
                    }
                    else
                    {
                        condition = condition.and((Condition) value);
                    }
                }
            }
        }

        return condition;
    }


    @Override
    public Object visit(ASTRelational node, Object data)
    {
        Field lft = ExpressionUtil.visit(node.jjtGetChild(0), this, Field.class);
        Field rgt = ExpressionUtil.visit(node.jjtGetChild(1), this, Field.class);

        switch(node.getOperator())
        {
            case LESS:
                return lft.lessThan(rgt);
            case LESS_OR_EQUALS:
                return lft.lessOrEqual(rgt);
            case GREATER:
                return lft.greaterThan(rgt);
            case GREATER_OR_EQUALS:
                return lft.greaterOrEqual(rgt);
            default:
                throw new IllegalStateException("Invalid operator: " + node.getOperator());
        }
    }

    @Override
    public Object visit(ASTEquality node, Object data)
    {
        Field lft = ExpressionUtil.visit(node.jjtGetChild(0), this, Field.class);
        Object rgt = ExpressionUtil.visit(node.jjtGetChild(1), this, Object.class);

        switch(node.getOperator())
        {
            case EQUALS:
                return lft.eq(rgt);
            case NOT_EQUALS:
                return lft.notEqual(rgt);
            default:
                throw new IllegalStateException("Invalid operator: " + node.getOperator());
        }
    }


    @Override
    public Object visit(ASTNot node, Object data)
    {
        return super.visit(node, data);
    }


    @Override
    public Object visit(ASTNegate node, Object data)
    {
        Field lft = ExpressionUtil.visit(node.jjtGetChild(0), this, Field.class);
        return DSL.not(lft);
    }


    @Override
    public Object visit(ASTIdentifier node, Object data)
    {
        return DSL.field( DSL.name(node.getName()));
    }


    @Override
    public Object visit(ASTPropertyChain propertyChainNode, Object data) throws UnsupportedOperationException
    {
        StringBuilder sb = new StringBuilder();

        // the chain starts with a function, we use the default resolution
        if (propertyChainNode.jjtGetNumChildren() > 0 && propertyChainNode.jjtGetChild(0) instanceof ASTFunction)
        {
            return super.visit(propertyChainNode, data);
        }


        // otherwise we just concatenate together the parts and convert it to a field.
        for (int i = 0; i < propertyChainNode.jjtGetNumChildren(); i++)
        {
            Node childNode = propertyChainNode.jjtGetChild(i);

            if (childNode instanceof ASTFunction)
            {
                throw new UnsupportedOperationException(((ASTFunction) childNode).getName());
            }
            if (i > 0)
            {
                sb.append(".");
            }
            sb.append(((ASTIdentifier)childNode).getName());

        }

        return DSL.field( DSL.name(sb.toString()));
    }


    @Override
    public Object undefinedOperation(ExpressionContext<ExpressionEnvironment> ctx, ASTFunction node, Object chainObject)
    {
        String operationName = node.getName();

        log.debug("Undefined in filter env: {} ({})", operationName, chainObject);

        if (chainObject instanceof Field && FieldOperations.contains(operationName) && node.jjtGetNumChildren() == 1)
        {
            return FieldOperations.execute(operationName, (Field)chainObject, node.jjtGetChild(0).jjtAccept(this, null));
        }

        if (transformerEnvironment != null)
        {
            return operationService.evaluate(transformerEnvironment, node, chainObject);
        }
        return super.undefinedOperation(ctx, node, chainObject);
    }


    public QueryTransformerEnvironment getTransformerEnvironment()
    {
        return transformerEnvironment;
    }


    public void setColumnContext(ComponentModel columnContext)
    {
        this.columnContext = columnContext;
    }


    public ComponentModel getColumnContext()
    {
        return columnContext;
    }


    public QueryDomainType getQueryDomainType()
    {
        return queryDomainType;
    }
}
