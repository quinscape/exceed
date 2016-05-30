package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ExpressionParserDefaultVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces the assignment operator with the equivalent set action.
 *
 */
public class AssignmentReplacementVisitor
    extends ExpressionParserDefaultVisitor
{
    private final static Logger log = LoggerFactory.getLogger(AssignmentReplacementVisitor.class);

    private final static SingleQuoteJSONGenerator generator = SingleQuoteJSONGenerator.INSTANCE;

    private boolean isValidScopeFunction(Node node)
    {
        // a valid scope function is an ASTFunction
        return (node instanceof ASTFunction &&
            // has a valid target name ("object"/"property"/"list")
            ExpressionUtil.validAssignmentTarget(((ASTFunction) node).getName()) &&
            // has exactly one child...
            node.jjtGetNumChildren() == 1 &&
            // which is a return
            node.jjtGetChild(0) instanceof ASTString);
    }

    @Override
    public Object visit(ASTAssignment node, Object data)
    {
        Node lft = node.jjtGetChild(0);

        Node scopeFn;
        String path;
        if (isValidScopeFunction(lft))
        {
            scopeFn = lft;
            path = null;
        }
        else if (isChainWithScopeFunction(lft))
        {
            scopeFn = lft.jjtGetChild(0);

            path = getIdentifierPath(lft, 1);
        }
        else
        {
            throw new IllegalStateException("Assignment left side must be scope function : " + ExpressionRenderer.render(node));
        }

        String name = ((ASTString) scopeFn.jjtGetChild(0)).getValue();

        Node rgt = node.jjtGetChild(1);

        int indexInParent = ExpressionUtil.findSiblingIndex(node);

        String fnName = ((ASTFunction) scopeFn).getName();

        try
        {
            String actionSource = "set({ type: " + generator.quote(fnName.toUpperCase()) +
                ", name: " + generator.quote(name) +
                ", value: null" +
                ", path: " + (path != null ? generator.quote(path) : "null") + "})";

            log.info("ACTION SOURCE: {}", actionSource);

            ASTFunction action = (ASTFunction) ExpressionParser.parse(actionSource).jjtGetChild(0);


            Node parent = node.jjtGetParent();
            parent.jjtAddChild(action, indexInParent);

            Node nameEntry = action.jjtGetChild(0).jjtGetChild(2);

            nameEntry.jjtAddChild(rgt, 1);
            rgt.jjtSetParent(nameEntry);

            Node pathEntry = action.jjtGetChild(0).jjtGetChild(2);

            pathEntry.jjtAddChild(rgt, 1);
            rgt.jjtSetParent(pathEntry);

            action.jjtSetParent(parent);

            // since action invocation cannot happen recursively, it is better to not
            // visit our new action but instead let funky expressions run into errors with nested, untransformed assignment expressions
            // (e,g. "property('x') = (property('x') =  1)" )
            return data;

        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    private String getIdentifierPath(Node lft, int start)
    {
        StringBuilder sb = new StringBuilder();

        int len = lft.jjtGetNumChildren();
        for (int i = start; i < len; i++)
        {
            if (i > start)
            {
                sb.append('.');
            }
            sb.append(((ASTIdentifier)lft.jjtGetChild(i)).getName());
        }

        return sb.toString();
    }


    /**
     * Returns true if the given node is a property chain starting with a scope accessor function followed
     * by only identifiers
     *
     * @param lft   potential property chain
     * @return  <code>true</code> the given node is a property chain starting with a scope accessor function followed
     *          by only identifiers
     */
    private boolean isChainWithScopeFunction(Node lft)
    {
        return lft instanceof ASTPropertyChain &&
            isValidScopeFunction(lft.jjtGetChild(0)) &&
            isIdentifierChain((ASTPropertyChain) lft, 1);
    }


    /**
     * Returns true if the given property chain only consists of identifier elements after the given starting point
     *
     * @param lft           property chain
     * @param start         start index
     * @return <code>true</code> if there are only identifier nodes after the start index
     */
    private boolean isIdentifierChain(ASTPropertyChain lft, int start)
    {
        int len = lft.jjtGetNumChildren();
        for (int i = start; i < len; i++)
        {
            if (!(lft.jjtGetChild(i) instanceof ASTIdentifier))
                return false;
        }
        return true;
    }


}
