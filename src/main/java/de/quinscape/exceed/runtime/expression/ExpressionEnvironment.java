package de.quinscape.exceed.runtime.expression;

import de.quinscape.exceed.expression.ASTAdd;
import de.quinscape.exceed.expression.ASTArray;
import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTDecimal;
import de.quinscape.exceed.expression.ASTDiv;
import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTExpressionSequence;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTLogicalAnd;
import de.quinscape.exceed.expression.ASTLogicalOr;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTMapEntry;
import de.quinscape.exceed.expression.ASTMult;
import de.quinscape.exceed.expression.ASTNegate;
import de.quinscape.exceed.expression.ASTNot;
import de.quinscape.exceed.expression.ASTNull;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTPropertyChainDot;
import de.quinscape.exceed.expression.ASTPropertyChainSquare;
import de.quinscape.exceed.expression.ASTRelational;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ASTSub;
import de.quinscape.exceed.expression.ExpressionParserVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.Operator;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Identifier;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.util.Util;

import javax.validation.constraints.Null;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base class for expression environments encapsulating the environment necessary for the expression
 * evaluation.
 *
 * @see ExpressionServiceImpl
 * @see ExpressionOperations
 *
 */
public abstract class ExpressionEnvironment
    implements ExpressionParserVisitor
{
    private final static Logger log = LoggerFactory.getLogger(ExpressionEnvironment.class);

    protected OperationService operationService;

    /** The name of the environment, used in error messages. */
    protected final String environmentName;

    private final static int PRECISION_INT = 0;
    private final static int PRECISION_LONG = 1;
    private final static int PRECISION_DECIMAL = 2;
    private final static int PRECISION_STRING = 3;

    void setOperationService(OperationService operationService)
    {
        this.operationService = operationService;
    }


    public ExpressionEnvironment()
    {
        Class<? extends ExpressionEnvironment> implementationClass = this.getClass();

        this.environmentName = implementationClass.getSimpleName();

        if ((implementationClass.getModifiers() & Modifier.PUBLIC) == 0)
        {
            throw new IllegalArgumentException("Expression environment implementation " + implementationClass + " is not declared as" +
                " public class");
        }
    }

    @Override
    public Object visit(ASTExpression node, Object data)
    {
        if (node.jjtGetNumChildren() != 1)
        {
            throw new ExpressionEnvironmentException("Query expressions must have exactly one property chain or " +
                "function child");
        }

        return node.jjtGetChild(0).jjtAccept(this, null);
    }


    @Override
    public Object visit(ASTPropertyChain node, Object data)
    {

        Node kid = node.jjtGetChild(0);

        Object chainObject = kid.jjtAccept(this, null);

        for (int i = 1; i < node.jjtGetNumChildren(); i++)
        {
            chainObject = node.jjtGetChild(i).jjtAccept(this, chainObject);
        }
        return chainObject;
    }
    

    @Override
    public Object visit(ASTFunction node, Object chainObject)
    {
        return operationService.evaluate(this, node, chainObject);
    }


    /**
     * Can be overriden to act when an operation name is not known within a context.
     *
     * The default implementation just throws an UnknownOperationException
     *
     *
     * @param ctx
     * @param node              function node
     * @param chainObject       current context
     *
     * @return  value of the unknown operation
     */
    public Object undefinedOperation(ExpressionContext<ExpressionEnvironment> ctx, ASTFunction node, Object chainObject)
    {
        throw new UnknownOperationException("Unknown operation '" + node.getName() + "' for environment " + this.environmentName + " and context = " + chainObject);
    }


    @SafeVarargs
    protected final Object visitOneChildOf(ASTFunction n, Class<? extends Node>... classes)
    {
        if (n.jjtGetNumChildren() != 1)
        {
            throw new ExpressionEnvironmentException("Expected exactly 1 argument of " + Arrays.toString(classes) +
                " for function " + n.getName() + "'");
        }

        Node firstArg = n.jjtGetChild(0);
        for (Class<? extends Node> cls : classes)
        {
            if (cls.isInstance(firstArg))
            {
                return firstArg.jjtAccept(this, null);
            }
        }

        throw new ExpressionEnvironmentException("Unexpected argument for function " + n.getName() + "': " +
            firstArg);
    }


    @SafeVarargs
    protected final SimpleNode expectChildOf(ASTFunction n, Class<? extends Node>... classes)
    {
        if (n.jjtGetNumChildren() != 1)
        {
            throw new ExpressionEnvironmentException("Expected exactly 1 argument of " + Arrays.toString(classes) +
                " for function " + n.getName() + "'");
        }

        SimpleNode firstArg = (SimpleNode) n.jjtGetChild(0);
        for (Class<? extends Node> cls : classes)
        {
            if (cls.isInstance(firstArg))
            {
                return firstArg;
            }
        }

        throw new ExpressionEnvironmentException("Unexpected argument for function " + n.getName() + "': " +
            firstArg);
    }

    // LITERAL NODES


    @Override
    public Object visit(ASTInteger node, Object data)
    {
        return node.getValue();
    }


    @Override
    public Object visit(ASTString node, Object data)
    {
        return node.getValue();
    }


    @Override
    public Object visit(ASTDecimal node, Object data)
    {
        return node.getValue();
    }


    @Override
    public Object visit(ASTBool node, Object data)
    {
        return node.getValue();
    }


    @Override
    public Object visit(ASTNull node, Object data)
    {
        return null;
    }

    // OPERATORS


    @Override
    public Object visit(ASTLogicalOr node, Object data)
    {
        if (!logicalOperatorsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Logical or operators not allowed.");
        }

        int numKids = node.jjtGetNumChildren();
        for (int i = 0; i < numKids; i++)
        {
            Node kid = node.jjtGetChild(i);

            Object result = kid.jjtAccept(this, null);
            if (result instanceof Boolean)
            {
                // bail on first true
                if (((Boolean) result))
                {
                    return true;
                }
            }
            else
            {
                throw new ExpressionEnvironmentException(environmentName + ": Cannot apply logical or to : " + result);
            }
        }
        return false;
    }




    @Override
    public Object visit(ASTLogicalAnd node, Object data)
    {
        if (!logicalOperatorsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Logical and operators not allowed.");
        }

        int numKids = node.jjtGetNumChildren();
        for (int i = 0; i < numKids; i++)
        {
            Node kid = node.jjtGetChild(i);

            Object result = kid.jjtAccept(this, null);
            if (result instanceof Boolean)
            {
                // bail on first false
                if (!((Boolean) result))
                {
                    return false;
                }
            }
            else
            {
                throw new ExpressionEnvironmentException(environmentName + ": Cannot apply logical and to : " + result);
            }
        }
        return true;
    }


    @Override
    public Object visit(ASTRelational node, Object data)
    {
        if (!comparatorsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Equality operators not allowed.");
        }

        Object lft = node.jjtGetChild(0).jjtAccept(this, null);
        Object rgt = node.jjtGetChild(1).jjtAccept(this, null);

        if (lft instanceof String && rgt instanceof String)
        {
            switch (node.getOperator())
            {

                case LESS:
                    return ((String) lft).compareTo((String) rgt) < 0;
                case LESS_OR_EQUALS:
                    return ((String) lft).compareTo((String) rgt) <= 0;
                case GREATER:
                    return ((String) lft).compareTo((String) rgt) > 0;
                case GREATER_OR_EQUALS:
                    return ((String) lft).compareTo((String) rgt) >= 0;
                default:
                    throw new ExpressionEnvironmentException("Invalid operator: " + node.getOperator());
            }
        }
        else if (lft instanceof BigDecimal && rgt instanceof BigDecimal)
        {
            BigDecimal vLft = toDecimal(lft);
            BigDecimal vRgt = toDecimal(rgt);

            switch (node.getOperator())
            {
                case LESS:
                    return vLft.compareTo(vRgt) < 0;
                case LESS_OR_EQUALS:
                    return vLft.compareTo(vRgt) <= 0;
                case GREATER:
                    return vLft.compareTo(vRgt) > 0;
                case GREATER_OR_EQUALS:
                    return vLft.compareTo(vRgt) >= 0;
                default:
                    throw new ExpressionEnvironmentException("Invalid operator " + node.getOperator());
            }
        }
        else if (lft instanceof Long || rgt instanceof Long)
        {
            double vLft = toLong(lft);
            double vRgt = toLong(rgt);

            switch (node.getOperator())
            {
                case LESS:
                    return vLft < vRgt;
                case LESS_OR_EQUALS:
                    return vLft <= vRgt;
                case GREATER:
                    return vLft > vRgt;
                case GREATER_OR_EQUALS:
                    return vLft >= vRgt;
                default:
                    throw new ExpressionEnvironmentException("Invalid operator " + node.getOperator());
            }
        }
        else if (lft instanceof Integer || rgt instanceof Integer)
        {
            int vLft = toInteger(lft);
            int vRgt = toInteger(rgt);

            switch (node.getOperator())
            {
                case LESS:
                    return vLft < vRgt;
                case LESS_OR_EQUALS:
                    return vLft <= vRgt;
                case GREATER:
                    return vLft > vRgt;
                case GREATER_OR_EQUALS:
                    return vLft >= vRgt;
                default:
                    throw new ExpressionEnvironmentException("Invalid operator " + node.getOperator());
            }
        }
        else
        {
            throw new ExpressionEnvironmentException("Cannot apply operator " + node.getOperator() + " to " + lft + ", " +
                "" + rgt);
        }
    }




    protected BigDecimal toDecimal(Object value)
    {
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        else if (value instanceof Long)
        {
            return new BigDecimal((Long)value);
        }
        else if (value instanceof Integer)
        {
            return new BigDecimal((Integer)value);
        }
        else
        {
            throw new ExpressionEnvironmentException("Cannot cast to double: " + value);
        }
    }


    protected long toLong(Object value)
    {
        if (value instanceof BigDecimal)
        {
            return ((BigDecimal) value).longValue();
        }
        else if (value instanceof Long)
        {
            return ((Long) value);
        }
        else if (value instanceof Integer)
        {
            return ((Integer) value).longValue();
        }
        else
        {
            throw new ExpressionEnvironmentException("Cannot cast to long: " + value);
        }
    }

    protected int toInteger(Object value)
    {
        if (value instanceof BigDecimal)
        {
            return ((BigDecimal) value).intValue();
        }
        else if (value instanceof Long)
        {
            return ((Long) value).intValue();
        }
        else if (value instanceof Integer)
        {
            return ((Integer) value);
        }
        else
        {
            throw new ExpressionEnvironmentException("Cannot cast to long: " + value);
        }
    }


    @Override
    public Object visit(ASTEquality node, Object data)
    {
        if (!comparatorsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Relational operators not allowed.");
        }

        Object lft = node.jjtGetChild(0).jjtAccept(this, null);
        Object rgt = node.jjtGetChild(1).jjtAccept(this, null);

        if (node.getOperator().equals(Operator.EQUALS))
        {
            return Util.equals(lft, rgt);
        }
        else
        {
            return !Util.equals(lft, rgt);
        }
    }


    @Override
    public Object visit(ASTAdd node, Object data)
    {
        if (!arithmeticOperatorsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Addition operators not allowed.");
        }

        int numKids = node.jjtGetNumChildren();
        List<Object> results = new ArrayList<>(numKids);

        int maxPrecision = findMaxPrecision(node, numKids, results);

        switch(maxPrecision)
        {
            // int
            case PRECISION_INT:
            {
                int number = toInteger(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number += toInteger(results.get(i));
                }
                return number;
            }
            // long
            case PRECISION_LONG:
            {

                long number = toLong(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number += toLong(results.get(i));
                }
                return number;
            }
            // decimal
            case PRECISION_DECIMAL:
            {
                BigDecimal number = toDecimal(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number = number.add(toDecimal(results.get(i)));
                }
                return number;
            }
            case PRECISION_STRING:
                StringBuilder sb = new StringBuilder();
                results.forEach(sb::append);
                return sb.toString();
            default:
                throw new IllegalStateException("Unhandled precision: " + maxPrecision);
        }
    }


    @Override
    public Object visit(ASTSub node, Object data)
    {
        if (!arithmeticOperatorsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Addition operators not allowed.");
        }

        int numKids = node.jjtGetNumChildren();
        List<Object> results = new ArrayList<>(numKids);

        int maxPrecision = findMaxPrecision(node, numKids, results);

        switch(maxPrecision)
        {
            // int
            case PRECISION_INT:
            {
                int number = toInteger(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number -= toInteger(results.get(i));
                }
                return number;
            }
            // long
            case PRECISION_LONG:
            {

                long number = toLong(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number -= toLong(results.get(i));
                }
                return number;
            }
            // decimal
            case PRECISION_DECIMAL:
            {
                BigDecimal number = toDecimal(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number = number.subtract(toDecimal(results.get(i)));
                }
                return number;
            }
            case PRECISION_STRING:
                throw new ExpressionEnvironmentException(environmentName + ": Cannot subtract strings");
            default:
                throw new IllegalStateException("Unhandled precision: " + maxPrecision);
        }
    }


    @Override
    public Object visit(ASTMult node, Object data)
    {
        if (!arithmeticOperatorsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Addition operators not allowed.");
        }

        int numKids = node.jjtGetNumChildren();
        List<Object> results = new ArrayList<>(numKids);

        int maxPrecision = findMaxPrecision(node, numKids, results);

        switch(maxPrecision)
        {
            // int
            case PRECISION_INT:
            {
                int number = toInteger(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number *= toInteger(results.get(i));
                }
                return number;
            }
            // long
            case PRECISION_LONG:
            {

                long number = toLong(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number *= toLong(results.get(i));
                }
                return number;
            }
            // decimal
            case PRECISION_DECIMAL:
            {
                BigDecimal number = toDecimal(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number = number.multiply(toDecimal(results.get(i)));
                }
                return number;
            }
            case PRECISION_STRING:
                throw new ExpressionEnvironmentException(environmentName + ": Cannot multiply strings");
            default:
                throw new IllegalStateException("Unhandled precision: " + maxPrecision);
        }
    }


    private int findMaxPrecision(Node node, int numKids, List<Object> results)
    {
        int maxPrecision = PRECISION_INT;
        for (int i = 0; i < numKids; i++)
        {
            Object operand = node.jjtGetChild(i).jjtAccept(this, null);

            if (operand instanceof String)
            {
                maxPrecision = Math.max(maxPrecision, PRECISION_STRING);
            }
            else if (operand instanceof BigDecimal)
            {
                maxPrecision = Math.max(maxPrecision, PRECISION_DECIMAL);
            }
            else if (operand instanceof Long)
            {
                maxPrecision = Math.max(maxPrecision, PRECISION_LONG);
            }
            else if (!(operand instanceof Integer))
            {
                throw new ExpressionEnvironmentException(environmentName + ": Invalid operand: " + operand);
            }
            
            results.add(operand);
        }
        return maxPrecision;
    }


    @Override
    public Object visit(ASTDiv node, Object data)
    {
        if (!arithmeticOperatorsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Addition operators not allowed.");
        }

        int numKids = node.jjtGetNumChildren();
        List<Object> results = new ArrayList<>(numKids);

        int maxPrecision = findMaxPrecision(node, numKids, results);

        switch(maxPrecision)
        {
            // int
            case PRECISION_INT:
            {
                int number = toInteger(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number /= toInteger(results.get(i));
                }
                return number;
            }
            // long
            case PRECISION_LONG:
            {

                long number = toLong(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number /= toLong(results.get(i));
                }
                return number;
            }
            // decimal
            case PRECISION_DECIMAL:
            {
                BigDecimal number = toDecimal(results.get(0));
                for (int i = 1; i < numKids; i++)
                {
                    number = number.divide(toDecimal(results.get(i)), number.scale(), BigDecimal.ROUND_HALF_EVEN);
                }
                return number;
            }
            case PRECISION_STRING:
                throw new ExpressionEnvironmentException(environmentName + ": Cannot divide strings");
            default:
                throw new IllegalStateException("Unhandled precision: " + maxPrecision);
        }
    }




    @Override
    public Object visit(ASTIdentifier node, Object data)
    {
        return operationService.resolveIdentifier(this, node.getName());
    }


    @Override
    public Object visit(SimpleNode node, Object data)
    {
        throw new IllegalStateException("Generic visitor should not be called. Do all AST classes implement the " +
            "accept method correctly?");
    }

    /**
     * Resolves an identifier embedded in the expression. This method by default just trows an
     * exception. If you want identifier resolution in your expression environment you should
     * first try to use the {@link Identifier} annotation, but you can also override this method
     * for a non-declarative, more flexible resolution.
     *
     * @param name identifier name
     * @return identifier value (default: throws exception)
     * @throws ExpressionEnvironmentException by default
     */
    public Object resolveIdentifier(String name)
    {
        throw new ExpressionEnvironmentException(environmentName + ": Unknown identifier '" + name + "'");
    }

    @Override
    public Object visit(ASTMap node, Object data)
    {
        if (!complexLiteralsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Invalid map literal");
        }

        Map<Object, Object> map = (Map)new HashMap<>();

        for (int i=0; i < node.jjtGetNumChildren(); i++)
        {
            Node kid = node.jjtGetChild(i);
            kid.jjtAccept(this, map);
        }

        return map;
    }


    @Override
    public Object visit(ASTArray node, Object data)
    {
        if (!complexLiteralsAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Invalid array literal");
        }

        List<Object> list = new ArrayList<>();

        for (int i=0; i < node.jjtGetNumChildren(); i++)
        {
            Node kid = node.jjtGetChild(i);

            list.add(kid.jjtAccept(this, list));
        }

        return list;
    }




    @Override
    public Object visit(ASTMapEntry node, Object data)
    {
        Node keyNode = node.jjtGetChild(0);
        Node ValueNode = node.jjtGetChild(1);

        String key;
        if (keyNode instanceof ASTIdentifier)
        {
            key = ((ASTIdentifier) keyNode).getName();
        }
        else
        {
            key = ((ASTString)keyNode).getValue();
        }

        Object value = ValueNode.jjtAccept(this, null);
        ((Map)data).put(key, value);

        return data;
    }


    @Override
    public Object visit(ASTNot node, Object data)
    {
        Object operand = node.jjtGetChild(0).jjtAccept(this, null);

        if (operand instanceof Boolean)
        {
            return !(Boolean) operand;
        }
        else if (operand instanceof Number)
        {
            return ((Number) operand).doubleValue() == 0;
        }
        else if (operand instanceof String)
        {
            return ((String) operand).length() == 0;
        }
        else
        {
            return false;
        }
    }


    @Override
    public Object visit(ASTNegate node, Object data)
    {
        Object operand = node.jjtGetChild(0).jjtAccept(this, null);

        if (operand instanceof Boolean)
        {
            return -((Boolean) operand ? 1: 0);
        }
        if (operand instanceof Integer)
        {
            return -((Integer) operand);
        }
        else if (operand instanceof Long)
        {
            return -((Long) operand);
        }
        else if (operand instanceof Double)
        {
            return -((Double) operand);
        }
        else
        {
            return Double.NaN;
        }
    }


//    @Override
//    public Object visit(ASTComputedPropertyChain node, Object data)
//    {
//        Node kid = node.jjtGetChild(0);
//
//        Object chainObject = kid.jjtAccept(this, null);
//
//        for (int i = 1; i < node.jjtGetNumChildren(); i++)
//        {
//            kid = node.jjtGetChild(i);
//            Object key = kid.jjtAccept(this, chainObject);
//
//            chainObject = JSONUtil.DEFAULT_UTIL.getProperty(chainObject, key.toString());
//        }
//        return chainObject;
//    }


    @Override
    public Object visit(ASTExpressionSequence node, Object data)
    {
        if (!expressionSequenceAllowed())
        {
            throw new ExpressionEnvironmentException(environmentName + ": Multiple expressions not allowed");
        }

        List<Object> list = new ArrayList<>();

        for (int i=0; i < node.jjtGetNumChildren(); i++)
        {
            Object value = node.jjtGetChild(i).jjtAccept(this, null);
            list.add(value);
        }

        return list;
    }


    @Override
    public Object visit(ASTAssignment node, Object data)
    {
        throw new ExpressionEnvironmentException(environmentName + ": Assignment operator not allowed.");
    }


    @Override
    public Object visit(ASTPropertyChainDot node, Object chainObject)
    {
        Node kid = node.jjtGetChild(0);

        if (kid instanceof ASTIdentifier)
        {
            String name = ((ASTIdentifier) kid).getName();
            if (chainObject == null)
            {
                throw new IllegalStateException("Cannot access property '" + name + "' of null");
            }
            return JSONUtil.DEFAULT_UTIL.getProperty(chainObject, name);
        }
        else if (kid instanceof ASTFunction)
        {
            return kid.jjtAccept(this, chainObject);
        }
        else
        {
            throw new IllegalStateException("Unexpected property chain dot child" + kid);
        }
    }


    @Override
    public Object visit(ASTPropertyChainSquare node, Object chainObject)
    {
        Node kid = node.jjtGetChild(0);

        Object result = kid.jjtAccept(this, null);

        if (result == null)
        {
            throw new IllegalStateException("Cannot access 'null' property of" + chainObject);
        }

        if (chainObject instanceof Collection)
        {
            if (!(result instanceof Number))
            {
                throw new IllegalStateException("Invalid index: " + result);
            }
            int index = ((Number) chainObject).intValue();
            if (chainObject instanceof List)
            {
                return ((List<?>)chainObject).get(index);
            }
            else
            {
                Iterator<?> iterator = ((Collection)chainObject).iterator();
                while (index-- >= 0)
                {
                    chainObject = iterator.next();
                }

                return chainObject;
            }
        }
        else
        {
            return JSONUtil.DEFAULT_UTIL.getProperty(chainObject, result.toString());
        }
    }


    protected abstract boolean logicalOperatorsAllowed();

    protected abstract boolean comparatorsAllowed();

    protected abstract boolean complexLiteralsAllowed();

    protected abstract boolean arithmeticOperatorsAllowed();

    protected abstract boolean expressionSequenceAllowed();




}
