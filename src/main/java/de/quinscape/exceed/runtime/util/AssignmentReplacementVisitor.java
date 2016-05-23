package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTFunction;
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


    @Override
    public Object visit(ASTAssignment node, Object data)
    {
        Node lft = node.jjtGetChild(0);
        if (!(lft instanceof ASTFunction) || !ExpressionUtil.validAssignmentTarget(((ASTFunction) lft).getName()) || !(lft.jjtGetChild(0) instanceof ASTString))
        {
            throw new IllegalStateException("Assignment left side must be scope function : " + ExpressionRenderer.render(node));
        }

        String name = ((ASTString) lft.jjtGetChild(0)).getValue();

        Node rgt = node.jjtGetChild(1);

        int indexInParent = ExpressionUtil.findSiblingIndex(node);

        String fnName = ((ASTFunction) lft).getName();

        try
        {
            ASTFunction action = (ASTFunction) ExpressionParser.parse("set({ type: '" + fnName.toUpperCase() + "', name: '" +
                name + "', value : " +
                "null})").jjtGetChild(0);


            Node parent = node.jjtGetParent();
            parent.jjtAddChild(action, indexInParent);

            Node entryNode = action.jjtGetChild(0).jjtGetChild(2);

            entryNode.jjtAddChild(rgt, 1);
            rgt.jjtSetParent(entryNode);

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


}
