package de.quinscape.exceed.runtime.expression;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.expression.ASTAdd;
import de.quinscape.exceed.expression.ASTBool;
import de.quinscape.exceed.expression.ASTDiv;
import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFloat;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ASTLogicalAnd;
import de.quinscape.exceed.expression.ASTLogicalOr;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTMapEntry;
import de.quinscape.exceed.expression.ASTMult;
import de.quinscape.exceed.expression.ASTNull;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTRelational;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ASTSub;
import de.quinscape.exceed.expression.ExpressionParserVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.Operator;
import de.quinscape.exceed.expression.SimpleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Helper base class to define expression language environments sharing the same expression language, AST classes and
 * node and token
 * structure but differing in the identifiers and functions provided.
 * <p>
 * Examines the actual implementations and registers all methods found that are public and take a ASTFunction node
 * as first parameter and an optional second parameter as context object
 * </p>
 * <p>
 * For each of the methods an expression operator function will be created:
 * </p>
 * <p>
 * <pre>
 * public static String foo(ASTNode node)
 * {
 * return "foo";
 * }
 * </pre>
 * <p>
 * <p>
 * would be called by a foo(...) expression whereas
 * </p>
 * <pre>
 * public static MyContext context(ASTNode MyContext)
 * {
 * return new MyContext();
 * }
 *
 * public static MyContext foo(ASTNode MyContext, MyContext myContext)
 * {
 * // do something with myContext
 *
 * return myContext;
 * }
 * </pre>
 * <p>
 * <p>
 * Would allow context() to create a context which is then chained to a foo() call for further manipulation. (e.g.
 * context('bla'").foo(12))
 * </p>
 */
public abstract class ExpressionEnvironment
    implements ExpressionParserVisitor
{
    private static Logger log = LoggerFactory.getLogger(ExpressionEnvironment.class);


    /**
     * Holds the methodsByContext maps for mapped by implementation class.
     */
    private final static ConcurrentMap<Class, EnvironmentHolder> allEnvironments = new ConcurrentHashMap<>();

    /**
     * Maps context types to maps of operation name to reflection method.
     *
     * Uses Void.class to represent "no context" / a function-arg only method.
     */
    private final Map<Class<?>, Map<String, Method>> methodsByContext;

    /**
     * Whether arithmetic operators are allowed in this environment
     */
    protected boolean arithmeticOperatorsAllowed = false;

    /**
     * Whether comparators are allowed in this environment
     */
    protected boolean comparatorsAllowed = false;

    /**
     * Whether logical operators are allowed in this environment
     */
    protected boolean logicalOperatorsAllowed = false;

    /**
     * Whether map literals are allowed in this environment
     */
    protected boolean mapLiteralsAllowed = false;


    /** The name of the environment, used in error messages. */
    protected String name;


    protected ExpressionEnvironment()
    {
        Class<? extends ExpressionEnvironment> implementationClass = this.getClass();

        this.name = implementationClass.getSimpleName();

        if ((implementationClass.getModifiers() & Modifier.PUBLIC) == 0)
        {
            throw new IllegalArgumentException("Expression environment implementation " + implementationClass + " is not declared as" +
                " public class");
        }

        /**
         * We look up the methodsByContext map from the concurrent map making
         * sure that we only analyze expression environments once per implementation type.
         */
        methodsByContext = lookupByType(implementationClass);
    }


    private Map<Class<?>, Map<String, Method>> lookupByType(Class<?> implementationClass)
    {
        EnvironmentHolder holder = new EnvironmentHolder(implementationClass);
        EnvironmentHolder existing = allEnvironments.putIfAbsent(implementationClass, holder);
        if (existing != null)
        {
            holder = existing;
        }
        return  holder.getMethodsByContext();
    }


    /**
     * Clears all cached environment operation maps.
     */
    public static void reset()
    {
        allEnvironments.clear();
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
        Object chainObject = null;
        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            Node kid = node.jjtGetChild(i);
            chainObject = propertyChainPart(kid, chainObject, i);
        }
        return chainObject;
    }


    /**
     * Called once for every child of an ASTPropertyChain.
     *
     * @param kid           current child
     * @param chainObject   current property chain object
     * @param index         index of the current child within the property chain
     *
     * @return  new / changed property chain object
     */
    protected Object propertyChainPart(Node kid, Object chainObject, int index)
    {
        if (kid instanceof ASTIdentifier)
        {
            chainObject = kid.jjtAccept(this, null);
        }
        if (kid instanceof ASTFunction)
        {
            chainObject = kid.jjtAccept(this, chainObject);

        }
        return chainObject;
    }


    @Override
    public Object visit(ASTFunction node, Object chainObject)
    {
        log.debug("Visit function node {}", node);


        Class<?> context = chainObject != null ? chainObject.getClass() : Void.class;

        Map<String, Method> fnMap = methodsByContext.get(context);
        if (fnMap == null)
        {
            throw new UnknownContextException("Environment " + this.name + " has no operations for context " +
                chainObject);
        }

        String operatorName = node.getName();
        Method method = fnMap.get(operatorName);


        if (method == null)
        {
            return undefinedOperation(node, chainObject);
        }

        try
        {
            if (context.equals(Void.class))
            {
                return method.invoke(this, node);
            }

            if (chainObject == null)
            {
                throw new IllegalStateException("Chain object shouldn't be null");
            }

            return method.invoke(this, node, chainObject);
        }
        catch (IllegalAccessException e)
        {
            throw new ExpressionEnvironmentException(this.name + ": error accessing " + method.getName());
        }
        catch (InvocationTargetException e)
        {
            throw new ExpressionEnvironmentException(operatorName + ": error calling operation '" + operatorName, e
                .getTargetException());
        }
    }


    /**
     * Can be overriden to act when an operation name is not known within a context.
     *
     * The default implementation just throws an UnknownOperationException
     *
     * @param node              function node
     * @param chainObject       current context
     *
     * @return  value of the unknown operation
     */
    protected Object undefinedOperation(ASTFunction node, Object chainObject)
    {
        throw new UnknownOperationException("Unknown operation for environment " + this.name + ", context = " + chainObject + ": " + node.getName());
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
    public Object visit(ASTFloat node, Object data)
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
        if (!logicalOperatorsAllowed)
        {
            throw new ExpressionEnvironmentException(name + ": Logical or operators not allowed in query expressions");
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
                throw new ExpressionEnvironmentException(name + ": Cannot apply logical or to : " + result);
            }
        }
        return false;
    }


    @Override
    public Object visit(ASTLogicalAnd node, Object data)
    {
        if (!logicalOperatorsAllowed)
        {
            throw new ExpressionEnvironmentException(name + ": Logical and operators not allowed in query expressions");
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
                throw new ExpressionEnvironmentException(name + ": Cannot apply logical and to : " + result);
            }
        }
        return true;
    }


    @Override
    public Object visit(ASTRelational node, Object data)
    {
        if (!comparatorsAllowed)
        {
            throw new ExpressionEnvironmentException(name + ": Equality operators not allowed in query expressions");
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
        else if (lft instanceof Double || rgt instanceof Double)
        {
            double vLft = toDouble(lft);
            double vRgt = toDouble(rgt);

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
        else if (lft instanceof Integer && rgt instanceof Integer)
        {
            int vLft = (Integer) lft;
            int vRgt = (Integer) rgt;

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


    protected double toDouble(Object value)
    {
        if (value instanceof Double)
        {
            return (double) value;
        }
        else if (value instanceof Long)
        {
            return ((Long) value).doubleValue();
        }
        else if (value instanceof Integer)
        {
            return ((Integer) value).doubleValue();
        }
        else
        {
            throw new ExpressionEnvironmentException("Cannot cast to double: " + value);
        }
    }


    protected long toLong(Object value)
    {
        if (value instanceof Double)
        {
            return (long) value;
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


    @Override
    public Object visit(ASTEquality node, Object data)
    {
        if (!comparatorsAllowed)
        {
            throw new ExpressionEnvironmentException(name + ": Relational operators not allowed in query expressions");
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
        if (!arithmeticOperatorsAllowed)
        {
            throw new ExpressionEnvironmentException(name + ": Addition operators not allowed in query expressions");
        }

        boolean stringAdd = false;
        boolean floatPrecision = false;

        int numKids = node.jjtGetNumChildren();
        List<Object> results = new ArrayList<>(numKids);
        for (int i = 0; i < numKids; i++)
        {

            Object operand;

            Node kid = node.jjtGetChild(i);
            if (kid instanceof ASTIdentifier)
            {
                operand = resolveIdentifier(((ASTIdentifier) kid).getName());
            }
            else if (kid instanceof ASTFunction)
            {
                operand = kid.jjtAccept(this, null);
            }
            else if (kid instanceof ASTString)
            {
                operand = ((ASTString) kid).getValue();
                stringAdd = true;
            }
            else if (kid instanceof ASTFloat)
            {
                operand = ((ASTFloat) kid).getValue();
                floatPrecision = true;
            }
            else if (kid instanceof ASTInteger)
            {
                operand = ((ASTInteger) kid).getValue();
            }
            else if (kid instanceof ASTBool)
            {
                operand = ((ASTBool) kid).getValue();
            }
            else if (kid instanceof ASTNull)
            {
                operand = null;
            }
            else
            {
                throw new ExpressionEnvironmentException("Cannot apply ADD to node " + kid);
            }

            results.add(operand);
        }

        if (stringAdd)
        {
            StringBuilder sb = new StringBuilder();
            results.forEach(sb::append);
            return sb.toString();
        }
        else
        {
            double value = 0;

            for (Object result : results)
            {
                if (result instanceof Integer)
                {
                    value += (Integer) result;
                }
                else if (result instanceof Long)
                {
                    value += (Long) result;
                }
                else if (result instanceof Double)
                {
                    value += (Double) result;
                }
                else
                {
                    throw new ExpressionEnvironmentException("Cannot add " + result + (result != null ? "( " + result
                        .getClass() + ")" : "") + " as number");
                }
            }

            if (floatPrecision)
            {
                return value;
            }
            else
            {
                return (int) value;
            }
        }
    }


    @Override
    public Object visit(ASTSub node, Object data)
    {
        if (!arithmeticOperatorsAllowed)
        {
            throw new ExpressionEnvironmentException(name + ": Addition operators not allowed in query expressions");
        }

        boolean floatPrecision = false;

        int numKids = node.jjtGetNumChildren();

        double value = 0;

        List<Object> results = new ArrayList<>(numKids);
        for (int i = 0; i < numKids; i++)
        {
            Object operand = node.jjtGetChild(i).jjtAccept(this, null);

            if (operand instanceof Integer)
            {
                Integer op = (Integer) operand;
                value = i == 0 ? op : value - op;
            }
            else if (operand instanceof Long)
            {
                Long op = (Long) operand;
                value = i == 0 ? op : value - op;
            }
            else if (operand instanceof Double)
            {
                Double op = (Double) operand;
                value = i == 0 ? op : value - op;
                floatPrecision = true;
            }
            else
            {
                throw new ExpressionEnvironmentException("Cannot subtract " + operand +
                    (operand != null ? "( " + operand.getClass() + ")" : "")
                    + " as number");
            }

        }
        if (floatPrecision)
        {
            return value;
        }
        else
        {
            return (int) value;
        }
    }


    @Override
    public Object visit(ASTMult node, Object data)
    {
        if (!arithmeticOperatorsAllowed)
        {
            throw new ExpressionEnvironmentException(name + ": Addition operators not allowed in query expressions");
        }

        boolean floatPrecision = false;

        int numKids = node.jjtGetNumChildren();

        double value = 0;

        List<Object> results = new ArrayList<>(numKids);
        for (int i = 0; i < numKids; i++)
        {
            Object operand = node.jjtGetChild(i).jjtAccept(this, null);

            if (operand instanceof Integer)
            {
                Integer op = (Integer) operand;
                value = i == 0 ? op : value * op;
            }
            else if (operand instanceof Long)
            {
                Long op = (Long) operand;
                value = i == 0 ? op : value * op;
            }
            else if (operand instanceof Double)
            {
                Double op = (Double) operand;
                value = i == 0 ? op : value * op;
                floatPrecision = true;
            }
            else
            {
                throw new ExpressionEnvironmentException("Cannot multiply " + operand +
                    (operand != null ? "( " + operand.getClass() + ")" : ""));
            }

        }
        if (floatPrecision)
        {
            return value;
        }
        else
        {
            return (int) value;
        }
    }


    @Override
    public Object visit(ASTDiv node, Object data)
    {
        if (!arithmeticOperatorsAllowed)
        {
            throw new ExpressionEnvironmentException(name + ": Addition operators not allowed in query expressions");
        }

        boolean floatPrecision = false;

        int numKids = node.jjtGetNumChildren();

        double value = 0;

        List<Object> results = new ArrayList<>(numKids);
        for (int i = 0; i < numKids; i++)
        {
            Object operand = node.jjtGetChild(i).jjtAccept(this, null);

            if (operand instanceof Integer)
            {
                Integer op = (Integer) operand;
                value = i == 0 ? op : ((int)value) / op;
            }
            else if (operand instanceof Long)
            {
                Long op = (Long) operand;
                value = i == 0 ? op : ((long)value) / op;
            }
            else if (operand instanceof Double)
            {
                Double op = (Double) operand;
                value = i == 0 ? op : value / op;
                floatPrecision = true;
            }
            else
            {
                throw new ExpressionEnvironmentException(name + ": Cannot multiply " + operand +
                    (operand != null ? "( " + operand.getClass() + ")" : ""));
            }

        }
        if (floatPrecision)
        {
            return value;
        }
        else
        {
            return (int) value;
        }
    }


    @Override
    public Object visit(ASTIdentifier node, Object data)
    {
        return resolveIdentifier(node.getName());
    }


    @Override
    public Object visit(SimpleNode node, Object data)
    {
        throw new IllegalStateException("Generic visitor should not be called. Do all AST classes implement the " +
            "accept method correctly?");
    }


    private static class EnvironmentHolder
    {
        private final Class<?> implementationClass;

        private volatile Map<Class<?>, Map<String, Method>> methodsByContext;


        private EnvironmentHolder(Class<?> implementationClass)
        {
            this.implementationClass = implementationClass;
        }


        public Map<Class<?>, Map<String, Method>> getMethodsByContext()
        {
            if (methodsByContext == null)
            {
                synchronized (this)
                {
                    if (methodsByContext == null)
                    {
                        methodsByContext = analyze();
                    }
                }
            }

            return methodsByContext;
        }


        private Map<Class<?>, Map<String, Method>> analyze()
        {
            log.debug("Analyzing {}", implementationClass);

            Map<Class<?>, Map<String, Method>> methodsByContext = new HashMap<>();
            for (Method m : implementationClass.getMethods())
            {
                Class<?>[] parameterTypes = m.getParameterTypes();

                // we're looking for all 2 argument methods that have ASTFunction as first parameter
                int numberOfParams = parameterTypes.length;
                if ((numberOfParams == 1 || numberOfParams == 2) && parameterTypes[0].equals(ASTFunction.class))
                {
                    Class<?> type = numberOfParams == 2 ? parameterTypes[1] : Void.class;
                    Map<String, Method> contextMap = methodsByContext.get(type);
                    if (contextMap == null)
                    {
                        contextMap = new HashMap<>();
                        methodsByContext.put(type, contextMap);
                    }
                    contextMap.put(m.getName(), m);
                }
            }

            // seal map after creation
            return ImmutableMap.copyOf(methodsByContext);
        }
    }


    /**
     * Resolves an identifier embedded in the expression. This method by default just trows an
     * exception. If you want identifier resolution in your expression environment you need
     * to override this method.
     *
     * @param name identifier name
     * @return identifier value (default: throws exception)
     * @throws ExpressionEnvironmentException by default
     */
    protected Object resolveIdentifier(String name)
    {
        throw new ExpressionEnvironmentException(name + ": Identifier resolution not enabled.");
    }


    /**
     * Provides a read-only map of context types to a map of operation names to implementation methods
     * for this environment.
     *
     * @return  methods by context map
     */
    public Map<Class<?>, Map<String, Method>> getMethodsByContext()
    {
        return methodsByContext;
    }


    @Override
    public Object visit(ASTMap node, Object data)
    {
        if (!mapLiteralsAllowed)
        {
            throw new ExpressionEnvironmentException(name + ": Invalid map literal");
        }

        Map<Object, Object> map = (Map)data;

        for (int i=0; i < node.jjtGetNumChildren(); i++)
        {
            Node kid = node.jjtGetChild(i);
            kid.jjtAccept(this, map);
        }

        return map;
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
}
