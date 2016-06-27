package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParserDefaultVisitor;
import de.quinscape.exceed.expression.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Expression visitor that collects scoped value references
 */
public class PropReferenceCollector
    extends ExpressionParserDefaultVisitor
{
    private final Set<String> references;


    public PropReferenceCollector()
    {
        references = new HashSet<>();
    }


    @Override
    public Object visit(ASTFunction node, Object data)
    {
        if (node.getName().equals("prop"))
        {
            Node n = node.jjtGetChild(0);
            if (!(n instanceof ASTString))
            {
                throw new IllegalStateException("First argument for function prop() is not a string literal");
            }
            String name = ((ASTString) n).getValue();
            references.add(name);
        }
        return null;
    }


    public Set<String> getReferences()
    {
        return references;
    }
}
