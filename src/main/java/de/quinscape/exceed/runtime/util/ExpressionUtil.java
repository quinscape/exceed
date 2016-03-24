package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ExpressionParserVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironmentException;

import java.util.Arrays;

public class ExpressionUtil
{

    @SafeVarargs
    public static Object visitOneChildOf(ExpressionParserVisitor visitor, ASTFunction n, Class<? extends Node>... classes)
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
                return firstArg.jjtAccept(visitor, null);
            }
        }

        throw new ExpressionEnvironmentException("Unexpected argument for function " + n.getName() + "': " +
            firstArg);
    }


    @SafeVarargs
    public static SimpleNode expectChildOf(ASTFunction n, Class<? extends Node>... classes)
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

    public static <T> T visit(Node node, ExpressionParserVisitor visitor, Class<T> cls)
    {
        Object value = node.jjtAccept(visitor, null);

        if (cls.isInstance(value))
        {
            return (T)value;
        }

        throw new IllegalArgumentException(value + " is no " + cls.getName());
    }

}
