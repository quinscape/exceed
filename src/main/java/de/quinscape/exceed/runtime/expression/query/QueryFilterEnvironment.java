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
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class QueryFilterEnvironment
    extends ExpressionEnvironment
{
    private static Logger log = LoggerFactory.getLogger(QueryFilterEnvironment.class);


    private final QueryTransformerEnvironment transformerEnvironment;

    private final QueryDomainType queryDomainType;

    private ComponentModel columnContext;

    private Set<DataField> fieldReferences = new HashSet<>();


    public QueryFilterEnvironment(
        QueryTransformerEnvironment transformerEnvironment,
        QueryDomainType queryDomainType
    )
    {
        this.transformerEnvironment = transformerEnvironment;
        this.queryDomainType = queryDomainType;
    }


    public Set<DataField> getFieldReferences()
    {
        return fieldReferences;
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
        final Field<Object> queryField = resolveDSLField(name);
        if (queryField != null)
        {
            return queryField;
        }

        final Object result = transformerEnvironment.resolveContextVariable(name);
        if (result != QueryTransformerEnvironment.NO_RESULT)
        {
            return result;
        }

        throw new InvalidExpressionException("Cannot resolve Identifier '" + name + "'");
    }


    private Field<Object> resolveDSLField(String name)
    {
        QueryDomainType queryDomainType = getQueryDomainType();
        DataField dataField = queryDomainType.findField(name);

        if (dataField == null)
        {
            return null;
        }
        
        addFieldReference(dataField);

        final RuntimeContext runtimeContext = getTransformerEnvironment().getRuntimeContext();

        NamingStrategy namingStrategy =  runtimeContext.getDomainService().getDataSource(queryDomainType.getType().getDataSourceName()).getStorageConfiguration().getNamingStrategy();

        return DSL.field(DSL.name(dataField.getNameFromStrategy(namingStrategy)));
    }


    private void addFieldReference(DataField dataField)
    {
        this.fieldReferences.add(dataField);
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
        final String name = node.getName();
        final Field<Object> queryField = resolveDSLField(
            name
        );

        if (queryField != null)
        {
            return queryField;
        }

        final Object result = transformerEnvironment.resolveContextVariable(name);
        if (result != QueryTransformerEnvironment.NO_RESULT)
        {
            return result;
        }

        throw new InvalidExpressionException("Cannot resolve " + ExpressionUtil.renderExpressionOf(node));
    }


    @Override
    public Object visit(ASTPropertyChain propertyChain, Object data) throws UnsupportedOperationException
    {
        StringBuilder sb = new StringBuilder();

        // the chain starts with a function, we use the default resolution
        if (propertyChain.jjtGetChild(0) instanceof ASTFunction)
        {
            return super.visit(propertyChain, data);
        }

        final int numLinks = propertyChain.jjtGetNumChildren();
        // otherwise we just concatenate together the parts and convert it to a field.
        for (int i = 0; i < numLinks; i++)
        {
            Node childNode = i == 0 ? propertyChain.jjtGetChild(0) : propertyChain.jjtGetChild(i).jjtGetChild(0);

            if (childNode instanceof ASTFunction)
            {
                final Field<Object> queryField = resolveDSLField(sb.toString());
                return undefinedOperation((ASTFunction) childNode, queryField);
            }
            if (i > 0)
            {
                sb.append(".");
            }
            sb.append(((ASTIdentifier)childNode).getName());
        }

        final Field<Object> queryField = resolveDSLField(sb.toString());

        if (queryField != null)
        {
            return queryField;
        }

        final Node chainChild = propertyChain.getChainChild(0);
        if (chainChild instanceof ASTIdentifier)
        {
            Object chainObj = transformerEnvironment.resolveContextVariable(((ASTIdentifier) chainChild).getName());
            if (chainObj != QueryTransformerEnvironment.NO_RESULT)
            {
                for (int i=1 ; i < numLinks; i++)
                {
                    chainObj = propertyChain.jjtAccept(transformerEnvironment, chainObj);
                }
                return chainObj;
            }
        }

        throw new InvalidExpressionException("Cannot resolve chain " + ExpressionUtil.renderExpressionOf(propertyChain));
    }


    @Override
    public Object undefinedOperation(ASTFunction node, Object chainObject)
    {
        String operationName = node.getName();

        log.debug("Undefined in filter env: {} ({})", operationName, chainObject);

        if (chainObject instanceof Field && FieldOperations.contains(operationName))
        {
            if (node.jjtGetNumChildren() == 0)
            {
                return FieldOperations.execute(operationName, (Field)chainObject, null);
            }
            else if (node.jjtGetNumChildren() == 1)
            {
                return FieldOperations.execute(operationName, (Field)chainObject, node.jjtGetChild(0).jjtAccept(this, null));
            } 
            else
            {
                throw new IllegalStateException("Invalid number of arguments for field operation: " + ExpressionUtil.renderExpressionOf(node));
            }
        }

        if (transformerEnvironment != null)
        {
            return operationService.evaluate(transformerEnvironment, node, chainObject);
        }
        return super.undefinedOperation(node, chainObject);
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
