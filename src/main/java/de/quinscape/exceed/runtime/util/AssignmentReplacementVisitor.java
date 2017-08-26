package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.expression.ASTAssignment;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ExpressionParserDefaultVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ScopeDeclaration;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.scope.ProcessContext;
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

    private final ScopeDeclarations definitions;

    public AssignmentReplacementVisitor(ApplicationModel applicationModel, String scopeLookupKey)
    {
        this.definitions = applicationModel.getMetaData().getScopeMetaModel().lookup(scopeLookupKey);
    }

    @Override
    public Object visit(ASTAssignment node, Object data)
    {
        Node lft = node.jjtGetChild(0);

        ASTIdentifier identifier;
        String path;
        if (lft instanceof ASTIdentifier)
        {
            identifier = (ASTIdentifier) lft;
            path = null;
        }
        else if (isIdentifierChain(lft))
        {
            identifier = null; //ExpressionUtil.getFirstIdentifier((PropertyChain) lft);

            path = getIdentifierPath(lft, 1);
        }
        else
        {
            throw new AssignmentReplacementException("Assignment left side must start with an identifier : " + ExpressionRenderer.render(node));
        }
        String name = identifier.getName();

        if (!name.equals(ProcessContext.DOMAIN_OBJECT_CONTEXT))
        {
            final ScopeDeclaration definition = definitions.get(name);
            if (definition == null)
            {
                throw new AssignmentReplacementException("Undefined scope identifier '" + name +"': " + ExpressionRenderer.render(node));
            }
        }

        Node rgt = node.jjtGetChild(1);

        int indexInParent = ExpressionUtil.findSiblingIndex(node);

        try
        {
            String actionSource = "set({ name: " + generator.quote(name) +
                ", value: null" +
                ", path: " + (path != null ? generator.quote(path) : "null") + "})";

            log.debug("ACTION SOURCE: {}", actionSource);

            ASTFunction action = (ASTFunction) ExpressionParser.parse(actionSource).jjtGetChild(0);

            Node parent = node.jjtGetParent();
            parent.jjtAddChild(action, indexInParent);

            Node nameEntry = action.jjtGetChild(0).jjtGetChild(1);

            nameEntry.jjtAddChild(rgt, 1);
            rgt.jjtSetParent(nameEntry);

            action.jjtSetParent(parent);

            // since action invocation cannot happen recursively, it is better to not
            // visit our new action but instead let funky expressions run into errors with nested, untransformed assignment expressions
            // (e,g. "x = (x =  1)" )
            return data;

        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    public static String getIdentifierPath(Node lft, int start)
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
    public static boolean isIdentifierChain(Node lft)
    {
        return lft instanceof ASTPropertyChain && firstIsIdentifier((ASTPropertyChain)lft);
    }


    private static boolean firstIsIdentifier(ASTPropertyChain lft)
    {
        Node n = lft;
        do
        {
            n = n.jjtGetChild(0);
            if (n instanceof ASTIdentifier)
            {
                return true;
            }

        } while (n instanceof ASTPropertyChain);
        return false;
    }


    /**
     * Returns true if the given property chain only consists of identifier elements after the given starting point
     *
     * @param lft           property chain
     * @param start         start index
     * @return <code>true</code> if there are only identifier nodes after the start index
     */
    public static boolean isIdentifierChain(ASTPropertyChain lft, int start)
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
