package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTExpressionSequence;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ExpressionParserVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironmentException;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.model.InvalidClientExpressionException;
import de.quinscape.exceed.runtime.service.ActionExpressionRenderer;

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


    /**
     * Makes sure that the given expression sequence either consists only of actions or of no action generators
     *
     * @param seq                          property chain
     * @param actionExpressionRenderer      action expression renderer
     * @return
     */
    public static boolean isChainOfActions(ASTExpressionSequence seq, ActionExpressionRenderer actionExpressionRenderer)
    {

        // Expression sequences must have more than 1 kids ( see Expression.jjt, ExpressionSequence() producer)
        Boolean isChainOfActions = isActionExpression(actionExpressionRenderer, seq.jjtGetChild(0));
        int i;
        for (i = 1; i < seq.jjtGetNumChildren(); i++)
        {
            Node kid = seq.jjtGetChild(i);

            if (isChainOfActions != isActionExpression(actionExpressionRenderer, kid))
            {
                throw new InvalidClientExpressionException("Expression sequence must be only action operations or have no action operations: " + ExpressionRenderer.render(seq));
            }
        }

        return isChainOfActions;
    }


    private static boolean isActionExpression(ActionExpressionRenderer actionExpressionRenderer, Node node)
    {
        boolean isAction = false;
        if (node instanceof ASTFunction)
        {
            String opName = ((ASTFunction) node).getName();
            isAction = actionExpressionRenderer.hasOperation(opName);
        }
        else if (node instanceof ASTAssignment)
        {
            /** @see #handleAssignmentAction(Node)  */
            throw new IllegalStateException("Unhandled assignment operator.");
        }
        return isAction;
    }


    public static boolean validAssignmentTarget(String name)
    {
        return name.equals("property") || name.equals("object") || name.equals("list");
    }


    /**
     * Converts assignments to scoped functions into the appropriate set action.
     *
     * @param ast       expression AST
     * @return transformed expression AST
     */
    public static <T extends Node> T handleAssignmentAction(T ast)
    {
        ast.jjtAccept(new AssignmentReplacementVisitor(), null);
        return ast;
    }


    /**
     * Returns the index the given node has among its siblings within the parent children.
     *
     * @param node  non-root node
     * @return index
     */
    public static int findSiblingIndex(Node node)
    {
        Node parent = node.jjtGetParent();

        if (parent == null)
        {
            throw new IllegalStateException("Node " + node + " has no parent");
        }

        for (int i = 0; i < parent.jjtGetNumChildren(); i++)
        {
            if (parent.jjtGetChild(i) == node)
            {
                return i;
            }
        }
        throw new IllegalStateException("Node not in children of parent");
    }


}
